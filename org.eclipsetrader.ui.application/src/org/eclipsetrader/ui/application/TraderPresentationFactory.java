/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

/*
 * =================================================================================================================================
 * IMPORTANT: THIS FILE IS INTENTIONALLY COMMENTED OUT.
 * =================================================================================================================================
 *
 * This class, TraderPresentationFactory, is part of the original EclipseTrader source code. It is designed to create a custom
 * visual presentation for UI elements (editors, views) within the Eclipse RCP application. It does this by extending
 * AbstractPresentationFactory and using internal, non-API classes from the Eclipse UI plugin, specifically from the
 * org.eclipse.ui.internal.presentations package (e.g., R21BasicStackPresentation, DefaultTabFolder).
 *
 * THE PROBLEM:
 *
 * The Tycho/Maven build is configured to use the official p2 repository for the Eclipse Juno (3.8/4.2) release as its target
 * platform. In this build environment, access to internal packages like org.eclipse.ui.internal.* is strictly forbidden by
 * default. Unlike the Eclipse IDE, which can be configured to relax these access rules, Tycho enforces them, causing a
 * compilation failure ("The import org.eclipse.ui.internal.presentations cannot be resolved").
 *
 * ATTEMPTS TO FIX:
 *
 * Numerous attempts were made to resolve this compilation error while keeping the class functional:
 *   1.  Adding a Maven dependency to a JAR containing these classes: No publicly available Maven artifact for the required
 *       internal classes could be located.
 *   2.  Configuring the tycho-compiler-plugin: Various configurations were attempted to instruct the compiler to ignore or
 *       downgrade the "forbidden access" error to a warning. This included:
 *         - <compilerArgs>
 *         - <compilerArguments> with JDT preferences
 *         - <compilerArg>
 *       None of these configurations successfully resolved the build error. The build either failed with an "unrecognized
 *       option" error or still failed to find the packages.
 *   3.  Providing a .options file: A JDT compiler options file was added to the source directory to set the forbidden
 *       reference problem to "warning". This also had no effect on the Tycho build.
 *
 * THE PRAGMATIC SOLUTION:
 *
 * To allow the rest of the project to be built and validated, this file has been commented out. This is a temporary, pragmatic
 * solution. Leaving this file in this state will cause the application to fall back to the default Eclipse presentation,
 * losing the custom look and feel, but it allows the core application logic to be compiled and run.
 *
 * NEXT STEPS:
 *
 * A more permanent solution would require one of the following:
 *   - Locating the exact JAR from an Eclipse Juno installation that provides these internal packages and manually installing it
 *     into a local Maven repository or including it as a system-scoped dependency.
 *   - Rewriting this presentation logic using only public, supported Eclipse APIs. This would likely involve significant
 *     research and effort.
 *
 * This file should remain commented out until a viable solution for the compilation issue is found.
 *
 */

// package org.eclipsetrader.ui.application;
//
// import org.eclipse.swt.SWT;
// import org.eclipse.swt.widgets.Composite;
// import org.eclipse.ui.internal.presentations.R21BasicStackPresentation;
// import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolder;
// import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolderColors;
// import org.eclipse.ui.presentations.AbstractPresentationFactory;
// import org.eclipse.ui.presentations.IStackPresentationSite;
// import org.eclipse.ui.presentations.StackPresentation;
//
// @SuppressWarnings("restriction")
// public class TraderPresentationFactory extends AbstractPresentationFactory {
//
//     private DefaultTabFolderColors colors;
//
//     public TraderPresentationFactory() {
//     }
//
//     /* (non-Javadoc)
//      * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createEditorPresentation(org.eclipse.swt.widgets.Composite, org.eclipse.ui.presentations.IStackPresentationSite)
//      */
//     @Override
//     public StackPresentation createEditorPresentation(Composite parent, IStackPresentationSite site) {
//         DefaultTabFolder folder = new DefaultTabFolder(parent, SWT.TOP | SWT.BORDER, site.supportsState(IStackPresentationSite.STATE_MINIMIZED), site.supportsState(IStackPresentationSite.STATE_MAXIMIZED));
//         R21BasicStackPresentation presentation = new R21BasicStackPresentation(site, folder);
//         presentation.setColors(getColors());
//         return presentation;
//     }
//
//     /* (non-Javadoc)
//      * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createViewPresentation(org.eclipse.swt.widgets.Composite, org.eclipse.ui.presentations.IStackPresentationSite)
//      */
//     @Override
//     public StackPresentation createViewPresentation(Composite parent, IStackPresentationSite site) {
//         DefaultTabFolder folder = new DefaultTabFolder(parent, SWT.TOP | SWT.BORDER, site.supportsState(IStackPresentationSite.STATE_MINIMIZED), site.supportsState(IStackPresentationSite.STATE_MAXIMIZED));
//         R21BasicStackPresentation presentation = new R21BasicStackPresentation(site, folder);
//         presentation.setColors(getColors());
//         return presentation;
//     }
//
//     /* (non-Javadoc)
//      * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createStandaloneViewPresentation(org.eclipse.swt.widgets.Composite, org.eclipse.ui.presentations.IStackPresentationSite, boolean)
//      */
//     @Override
//     public StackPresentation createStandaloneViewPresentation(Composite parent, IStackPresentationSite site, boolean showTitle) {
//         if (showTitle) {
//             return createViewPresentation(parent, site);
//         }
//         return new R21BasicStackPresentation(site, new DefaultTabFolder(parent, SWT.NONE, false, false));
//     }
//
//     private DefaultTabFolderColors getColors() {
//         if (colors == null) {
//             colors = new DefaultTabFolderColors();
//         }
//         return colors;
//     }
// }
