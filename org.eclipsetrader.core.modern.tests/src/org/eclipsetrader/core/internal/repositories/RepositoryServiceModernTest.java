package org.eclipsetrader.core.internal.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListElement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnitPlatform.class)
public class RepositoryServiceModernTest {

    private Map<String, RepositoryMock> repositories;
    private volatile RepositoryResourceDelta[] lastDeltas;

    class RepositoryServiceMock extends RepositoryService {
        @Override
        public IRepository getRepository(String scheme) {
            return repositories.get(scheme);
        }
    }

    class RepositoryMock implements IRepository {
        private final String scheme;
        public final List<StoreMock> stores;
        RepositoryMock(String scheme) {
            this.scheme = scheme;
            this.stores = new ArrayList<StoreMock>();
        }
        @Override
        public String getSchema() {
            return scheme;
        }
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            return null;
        }
        @Override
        public boolean canDelete() {
            return true;
        }
        @Override
        public boolean canWrite() {
            return true;
        }
        @Override
        public IStore createObject() {
            try {
                StoreMock store = new StoreMock(this, new URI(scheme, "object", String.valueOf(stores.size() + 1)));
                stores.add(store);
                return store;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public IStore[] fetchObjects(IProgressMonitor monitor) {
            return new IStore[0];
        }
        @Override
        public IStore getObject(URI uri) {
            return null;
        }
        @Override
        public IStatus runInRepository(IRepositoryRunnable runnable, IProgressMonitor monitor) {
            try {
                runnable.run(monitor);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Status.OK_STATUS;
        }
        @Override
        public IStatus runInRepository(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
            try {
                runnable.run(monitor);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Status.OK_STATUS;
        }
        @Override
        public List<IStore> getTradesFor(ISecurity security) {
            return new ArrayList<IStore>();
        }
    }

    class StoreMock implements IStore {
        private final URI uri;
        private final RepositoryMock repository;
        private IStoreProperties properties;
        StoreMock(RepositoryMock repository, URI uri) {
            this.repository = repository;
            this.uri = uri;
        }
        @Override
        public void delete(IProgressMonitor monitor) throws CoreException {
            repository.stores.remove(this);
        }
        @Override
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
            return properties;
        }
        @Override
        public IStore[] fetchChilds(IProgressMonitor monitor) {
            List<IStore> l = new ArrayList<IStore>();
            String type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);
            if (ISecurity.class.getName().equals(type)) {
                for (StoreMock store : repository.stores) {
                    IStoreProperties storeProperties = store.fetchProperties(monitor);
                    if (IHistory.class.getName().equals(storeProperties.getProperty(IPropertyConstants.OBJECT_TYPE))) {
                        Security security = (Security) storeProperties.getProperty(IPropertyConstants.SECURITY);
                        if (security.getStore().toURI().equals(toURI())) {
                            l.add(store);
                        }
                    }
                }
            }
            return l.toArray(new IStore[l.size()]);
        }
        @Override
        public IStore createChild() {
            return null;
        }
        @Override
        public IRepository getRepository() {
            return repository;
        }
        @Override
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
            this.properties = properties;
        }
        @Override
        public URI toURI() {
            return uri;
        }
    }

    private void setUp() {
        repositories = new HashMap<String, RepositoryMock>();
        repositories.put("local", new RepositoryMock("local"));
        repositories.put("remote", new RepositoryMock("remote"));
        lastDeltas = null;
    }

    @Test
    void testSaveSecurity() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(1, repositories.get("local").stores.size());
        Assertions.assertEquals(0, repositories.get("remote").stores.size());
        Assertions.assertSame(repositories.get("local").stores.get(0), security.getStore());
    }

    @Test
    void testMoveSecurity() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(1, repositories.get("local").stores.size());
        Assertions.assertEquals(0, repositories.get("remote").stores.size());
        service.moveAdaptable(new ISecurity[] { security }, repositories.get("remote"));
        Assertions.assertEquals(0, repositories.get("local").stores.size());
        Assertions.assertEquals(1, repositories.get("remote").stores.size());
        Assertions.assertSame(repositories.get("remote").stores.get(0), security.getStore());
    }

    @Test
    void testDeleteSecurity() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(1, repositories.get("local").stores.size());
        service.deleteAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(0, repositories.get("local").stores.size());
        Assertions.assertNull(security.getStore());
    }

    @Test
    void testSaveSecurityAndAddToCollection() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        Assertions.assertEquals(0, service.getSecurities().length);
        service.saveAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(1, service.getSecurities().length);
        Assertions.assertSame(security, service.getSecurities()[0]);
    }

    @Test
    void testDeleteSecurityAndRemoveFromCollection() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(1, service.getSecurities().length);
        service.deleteAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(0, service.getSecurities().length);
    }

    @Test
    void testMoveSecurityChangesURI() throws Exception {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] { security });
        Assertions.assertEquals(new URI("local", "object", "1"), security.getStore().toURI());
        service.moveAdaptable(new ISecurity[] { security }, repositories.get("remote"));
        Assertions.assertEquals(new URI("remote", "object", "1"), security.getStore().toURI());
    }

    @Test
    void testSaveSecurityEvent() {
        setUp();
        final Security security = new Security("Security", new FeedIdentifier("ID", null));
        final RepositoryService service = new RepositoryServiceMock();
        service.addRepositoryResourceListener(new IRepositoryChangeListener() {
            @Override
            public void repositoryResourceChanged(RepositoryChangeEvent event) {
                lastDeltas = event.getDeltas();
            }
        });
        service.runInService(new IRepositoryRunnable() {
            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.saveAdaptable(new ISecurity[] { security });
                return Status.OK_STATUS;
            }
        }, null);
        Assertions.assertNotNull(lastDeltas);
        Assertions.assertEquals(1, lastDeltas.length);
        Assertions.assertSame(security, lastDeltas[0].getResource());
        Assertions.assertSame(repositories.get("local"), lastDeltas[0].getMovedTo());
    }

    @Test
    void testMoveSecurityEvent() {
        setUp();
        final Security security = new Security("Security", new FeedIdentifier("ID", null));
        final RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] { security });
        service.addRepositoryResourceListener(new IRepositoryChangeListener() {
            @Override
            public void repositoryResourceChanged(RepositoryChangeEvent event) {
                lastDeltas = event.getDeltas();
            }
        });
        service.runInService(new IRepositoryRunnable() {
            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.moveAdaptable(new ISecurity[] { security }, repositories.get("remote"));
                return Status.OK_STATUS;
            }
        }, null);
        Assertions.assertNotNull(lastDeltas);
        Assertions.assertEquals(1, lastDeltas.length);
        Assertions.assertSame(security, lastDeltas[0].getResource());
        Assertions.assertSame(repositories.get("local"), lastDeltas[0].getMovedFrom());
        Assertions.assertSame(repositories.get("remote"), lastDeltas[0].getMovedTo());
    }

    @Test
    void testDeleteSecurityEvent() {
        setUp();
        final Security security = new Security("Security", new FeedIdentifier("ID", null));
        final RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] { security });
        service.addRepositoryResourceListener(new IRepositoryChangeListener() {
            @Override
            public void repositoryResourceChanged(RepositoryChangeEvent event) {
                lastDeltas = event.getDeltas();
            }
        });
        service.runInService(new IRepositoryRunnable() {
            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.deleteAdaptable(new ISecurity[] { security });
                return Status.OK_STATUS;
            }
        }, null);
        Assertions.assertNotNull(lastDeltas);
        Assertions.assertEquals(1, lastDeltas.length);
        Assertions.assertSame(security, lastDeltas[0].getResource());
        Assertions.assertSame(repositories.get("local"), lastDeltas[0].getMovedFrom());
        Assertions.assertNull(lastDeltas[0].getMovedTo());
    }

    @Test
    void testDeleteSecurityRemovesFromWatchList() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        WatchList list = new WatchList("List", new IWatchListColumn[0]);
        list.setItems(new IWatchListElement[] { new WatchListElement(security) });
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new IAdaptable[] { security, list });
        Assertions.assertEquals(2, repositories.get("local").stores.size());
        service.deleteAdaptable(new IAdaptable[] { security });
        Assertions.assertEquals(1, repositories.get("local").stores.size());
        Assertions.assertEquals(0, list.getItemCount());
    }

    @Test
    void testMoveSecurityWithHistory() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        History history = new History(security, new IOHLC[0]);
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new IAdaptable[] { security, history });
        service.moveAdaptable(new IAdaptable[] { security }, repositories.get("remote"));
        Assertions.assertEquals(0, repositories.get("local").stores.size());
        Assertions.assertEquals(2, repositories.get("remote").stores.size());
    }

    @Test
    void testGetHistoryForSecurity() {
        setUp();
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        History history = new History(security, new IOHLC[0]);
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new IAdaptable[] { security, history });
        Assertions.assertNotNull(service.getHistoryFor(security));
    }
}
