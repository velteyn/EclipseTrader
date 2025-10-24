package org.eclipsetrader.ui.internal.presentation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
/*
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultSimpleTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolder;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultThemeListener;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
*/
import org.eclipse.ui.presentations.IStackPresentationSite;
//import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

public class TraderPresentationFactory /*extends WorkbenchPresentationFactory*/ {
    public static final String PRESENTATION_ID = "org.eclipsetrader.ui.presentation"; //$NON-NLS-1$

    private IWorkbenchConfigurer configurer;

    private int viewTabPosition = SWT.TOP;

    public TraderPresentationFactory() {
    }
/*
    public StackPresentation createViewPresentation(Composite parent, IStackPresentationSite site) {

        DefaultTabFolder folder = new DefaultTabFolder(parent, viewTabPosition | SWT.BORDER, site.supportsState(IStackPresentationSite.STATE_MINIMIZED), site.supportsState(IStackPresentationSite.STATE_MAXIMIZED));

        folder.setSimpleTabs(viewTabPosition == SWT.TOP);

        final int minimumCharacters = 4;
        folder.setMinimumCharacters(minimumCharacters);

        PresentablePartFolder partFolder = new PresentablePartFolder(folder);

        // create the presentation
        final WorkbenchAdvisor advisor = configurer.getWorkbenchAdvisor();

        TabbedStackPresentation result = new TabbedStackPresentation(site, partFolder, new StandardViewSystemMenu(site));

        DefaultThemeListener themeListener = new DefaultThemeListener(folder, result.getTheme());
        result.getTheme().addPropertyChangeListener(themeListener);

        new DefaultSimpleTabListener(result.getApiPreferences(), IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, folder);

        return result;
    }
    */
}
