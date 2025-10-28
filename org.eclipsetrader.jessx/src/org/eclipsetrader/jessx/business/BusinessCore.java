
package org.eclipsetrader.jessx.business;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.util.Currency;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.jessx.business.event.AssetEvent;
import org.eclipsetrader.jessx.business.event.AssetListener;
import org.eclipsetrader.jessx.business.event.InstitutionEvent;
import org.eclipsetrader.jessx.business.event.InstitutionListener;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.utils.Utils;
import org.jdom.Content;
import org.jdom.Element;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public abstract class BusinessCore {
	private static HashMap institutions = new HashMap<Object, Object>();

	private static HashMap assets = new HashMap<Object, Object>();

	private static Scenario scenario = new Scenario();

	private static Element chatNode;

	private static Vector assetsListeners = new Vector();

	private static Vector institutionsListeners = new Vector();

	private static GeneralParameters generalParam;

	public static void setGeneralParameters(GeneralParameters genParams) {
		generalParam = genParams;
	}

	public static GeneralParameters getGeneralParameters() {
		return generalParam;
	}

	public static Scenario getScenario() {
		return scenario;
	}

	public static Institution getInstitution(String institutionName) {
		return (Institution) institutions.get(institutionName);
	}

	public static HashMap getInstitutions() {
		return institutions;
	}

	public static Asset getAsset(String assetName) {
		return (Asset) assets.get(assetName.trim());
	}

	public static HashMap getAssets() {
		return assets;
	}

	public static void addAsset(Asset asset) {
		if (asset != null) {
			assets.put(asset.getAssetName(), asset);
			fireAssetAdded(asset);
		}
	}

	public static void removeAsset(Asset asset) {
		if (asset != null && assets.containsValue(asset)) {
			assets.remove(asset.getAssetName());
			fireAssetRemoved(asset);
		}
	}

	public static void addInstitution(Institution institution) {
		if (institution != null) {
			institutions.put(institution.getName(), institution);
			fireInstitutionAdded(institution);
		}
	}

	public static void removeInstitution(Institution institution) {
		if (institution != null && institutions.containsValue(institution)) {
			institutions.remove(institution.getName());
			fireInstitutionRemoved(institution);
		}
	}

	public static void saveToXml(Element rootNode) {
		Element genParamNode = new Element("GeneralParameters");
		getGeneralParameters().saveToXml(genParamNode);
		rootNode.addContent((Content) genParamNode);
		Vector<String> assetKeys = Utils.convertAndSortMapToVector(getAssets());
		int keysCount = assetKeys.size();
		for (int i = 0; i < keysCount; i++) {
			Element asset = new Element("Asset");
			Asset.saveAssetToXml(asset, getAsset(assetKeys.get(i)));
			rootNode.addContent((Content) asset);
		}
		Vector<String> instKeys = Utils.convertAndSortMapToVector(getInstitutions());
		keysCount = instKeys.size();
		for (int j = 0; j < keysCount; j++) {
			Element institution = new Element("Institution");
			Institution.saveInstitutionToXml(institution, getInstitution(instKeys.get(j)));
			rootNode.addContent((Content) institution);
		}
		Element scenarioNode = new Element("Scenario");
		getScenario().saveToXml(scenarioNode);
		rootNode.addContent((Content) scenarioNode);
		chatNode = new Element("Chat");
		rootNode.addContent((Content) chatNode);
	}

	public static Element getElementToSaveChat() {
		return chatNode;
	}

	public static void loadFromXml(Element root) {
		Element genParam = root.getChild("GeneralParameters");
		if (genParam == null) {
			Utils.logger.error("Invalid xml format: GeneralParameters nodes not found.");
			return;
		}
		getGeneralParameters().loadFromXml(genParam);

        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference == null) {
            Utils.logger.error("IRepositoryService not found.");
            return;
        }
        IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);

        ISecurity[] securities = repositoryService.getSecurities();
        Map<String, ISecurity> securitiesMap = new HashMap<String, ISecurity>();
        for (ISecurity security : securities) {
            securitiesMap.put(security.getName().trim(), security);
        }

		final IRepositoryService finalRepositoryService = repositoryService;
		Iterator<Element> assetNodes = root.getChildren("Asset").iterator();
		while (assetNodes.hasNext()) {
			final Asset asset = Asset.loadAssetFromXml(assetNodes.next());
            final String assetName = asset.getAssetName().trim();
            ISecurity security = securitiesMap.get(assetName);
            if (security == null) {
                final CountDownLatch latch = new CountDownLatch(1);
                repositoryService.runInService(new IRepositoryRunnable() {
                    @Override
                    public IStatus run(IProgressMonitor monitor) {
                        try {
                            IStore store = finalRepositoryService.getRepository("hibernate").createObject();
                            IStoreProperties properties = store.fetchProperties(monitor);
                            properties.setProperty(IPropertyConstants.OBJECT_TYPE, Stock.class.getName());
                            properties.setProperty(IPropertyConstants.NAME, assetName);
                            properties.setProperty(IPropertyConstants.CURRENCY, Currency.getInstance("USD"));

                            store.putProperties(properties, monitor);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            latch.countDown();
                        }
                        return Status.OK_STATUS;
                    }
                }, null);

                try {
                    latch.await();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                securities = repositoryService.getSecurities();
                for (ISecurity s : securities) {
                    securitiesMap.put(s.getName().trim(), s);
                }
                security = securitiesMap.get(assetName);
            }
            asset.setSecurity(security);
			addAsset(asset);
		}

        context.ungetService(serviceReference);

		Iterator<Element> institutionNodes = root.getChildren("Institution").iterator();
		while (institutionNodes.hasNext()) {
			Institution institution = Institution.loadInstitutionFromXml(institutionNodes.next());
			addInstitution(institution);
		}

        for (Object obj : institutions.values()) {
            Institution institution = (Institution) obj;
            final String assetName = institution.getAssetName().trim();
            if (assets.get(assetName) == null) {
                Asset asset = new Asset() {
                    @Override
                    public String getAssetName() {
                        return assetName;
                    }
                    @Override
                    public void saveToXml(Element root) {
                    }
                    @Override
                    public void loadFromXml(Element root) {
                    }
                    @Override
                    public JPanel getAssetSetupGui() {
                        return null;
                    }
                };

                ISecurity security = securitiesMap.get(assetName);
                if (security == null) {
                    final CountDownLatch latch = new CountDownLatch(1);
                    repositoryService.runInService(new IRepositoryRunnable() {
                        @Override
                        public IStatus run(IProgressMonitor monitor) {
                            try {
                                IStore store = finalRepositoryService.getRepository("hibernate").createObject();
                                IStoreProperties properties = store.fetchProperties(monitor);
                                properties.setProperty(IPropertyConstants.OBJECT_TYPE, Stock.class.getName());
                                properties.setProperty(IPropertyConstants.NAME, assetName);
                                properties.setProperty(IPropertyConstants.CURRENCY, Currency.getInstance("USD"));

                                store.putProperties(properties, monitor);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                latch.countDown();
                            }
                            return Status.OK_STATUS;
                        }
                    }, null);

                    try {
                        latch.await();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    securities = repositoryService.getSecurities();
                    for (ISecurity s : securities) {
                        securitiesMap.put(s.getName().trim(), s);
                    }
                    security = securitiesMap.get(assetName);
                }
                asset.setSecurity(security);
                addAsset(asset);
            }
        }
		Element scenario = root.getChild("Scenario");
		if (scenario == null) {
			Utils.logger.error("Invalid xml files: scenario node not found.");
			return;
		}
		getScenario().loadFromXml(scenario);
	}

	protected static void fireAssetAdded(Asset asset) {
		for (int i = 0; i < assetsListeners.size(); i++)
			((AssetListener) assetsListeners.elementAt(i)).assetsModified(new AssetEvent(asset.getAssetName(), 1));
	}

	protected static void fireAssetRemoved(Asset asset) {
		for (int i = 0; i < assetsListeners.size(); i++)
			((AssetListener) assetsListeners.elementAt(i)).assetsModified(new AssetEvent(asset.getAssetName(), 0));
	}

	protected static void fireInstitutionAdded(Institution institution) {
		for (int i = 0; i < institutionsListeners.size(); i++)
			((InstitutionListener) institutionsListeners.elementAt(i)).institutionsModified(new InstitutionEvent(institution.getName(), 1));
	}

	protected static void fireInstitutionRemoved(Institution institution) {
		for (int i = 0; i < institutionsListeners.size(); i++)
			((InstitutionListener) institutionsListeners.elementAt(i)).institutionsModified(new InstitutionEvent(institution.getName(), 0));
	}

	public static void addAssetListener(AssetListener listener) {
		assetsListeners.add(listener);
	}

	public static void addInstitutionListener(InstitutionListener listener) {
		institutionsListeners.add(listener);
	}

	public static void removeAssetListener(AssetListener listener) {
		assetsListeners.remove(listener);
	}

	public static void removeInstitutionListener(InstitutionListener listener) {
		institutionsListeners.remove(listener);
	}
}
