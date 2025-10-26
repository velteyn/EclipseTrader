/*
package org.eclipsetrader.ui.internal.presentation;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultSimpleTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolder;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultThemeListener;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

@SuppressWarnings("restriction")
public class TraderPresentationFactory extends WorkbenchPresentationFactory {

    private int viewTabPosition = SWT.TOP;

    private IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION)) {
                viewTabPosition = PrefUtil.getAPIPreferenceStore().getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);
            }
        }
    };

    public TraderPresentationFactory() {
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        apiStore.addPropertyChangeListener(preferenceListener);
        viewTabPosition = apiStore.getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);
    }

    @Override
    public StackPresentation createViewPresentation(Composite parent, IStackPresentationSite site) {
        DefaultTabFolder folder = new DefaultTabFolder(parent, viewTabPosition | SWT.BORDER, site.supportsState(IStackPresentationSite.STATE_MINIMIZED), site.supportsState(IStackPresentationSite.STATE_MAXIMIZED));
        folder.setUnselectedCloseVisible(false);
        folder.setUnselectedImageVisible(true);
        final Color unselectedTabColor = new Color(parent.getDisplay(), 255, 0, 0);
        folder.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                unselectedTabColor.dispose();
            }
        });
        // folder.setUnselectedTabColor(unselectedTabColor);

        PresentablePartFolder partFolder = new PresentablePartFolder(folder);

        Control control = partFolder.getControl();
        control.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });

        TabbedStackPresentation result = new TabbedStackPresentation(site, partFolder, new StandardViewSystemMenu(site));

        DefaultThemeListener themeListener = new DefaultThemeListener(folder, result.getTheme());
        result.addPropertyChangeListener(themeListener);

        new DefaultSimpleTabListener(result.getApiPreferences(), IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, folder);

        return result;
    }

    @Override
    public void dispose() {
        PrefUtil.getAPIPreferenceStore().removePropertyChangeListener(preferenceListener);
        super.dispose();
    }
}
*/
