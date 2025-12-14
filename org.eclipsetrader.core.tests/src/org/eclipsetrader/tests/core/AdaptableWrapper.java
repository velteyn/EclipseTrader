package org.eclipsetrader.tests.core;

import org.eclipse.core.runtime.IAdaptable;

public class AdaptableWrapper implements IAdaptable {

    private Object element;

    public AdaptableWrapper(Object element) {
        this.element = element;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (element != null && adapter.isAssignableFrom(element.getClass())) {
            return element;
        }
        return null;
    }
}
