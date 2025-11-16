package org.eclipsetrader.ui.internal.markets;

import java.util.Date;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;

public class CDateTimeCellEditorSimple extends CellEditor {

    private final int style;
    private CDateTime control;

    public CDateTimeCellEditorSimple(Composite parent, int style) {
        super(parent, SWT.NONE);
        this.style = style;
    }

    @Override
    protected Control createControl(Composite parent) {
        control = new CDateTime(parent, style);
        return control;
    }

    @Override
    protected Object doGetValue() {
        return control.getSelection();
    }

    @Override
    protected void doSetFocus() {
        if (control != null && !control.isDisposed()) {
            control.setFocus();
        }
    }

    @Override
    protected void doSetValue(Object value) {
        if (value instanceof Date) {
            control.setSelection((Date) value);
        }
    }
}