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

package org.eclipsetrader.repository.local.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class RepositoryFactory implements IExecutableExtensionFactory {

    public RepositoryFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    @Override
    public Object create() throws CoreException {
        Activator activator = Activator.getDefault();
        if (activator == null) {
            throw new CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, Activator.PLUGIN_ID, "Local Repository Activator is not available (bundle may have failed to start)."));
        }
        return activator.getRepository();
    }
}
