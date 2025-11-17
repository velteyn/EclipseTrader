package org.eclipsetrader.ui.internal.markets;

import java.util.Date;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;

public class CDateTimeCellEditorSimple extends CellEditor {

    private final int style;
    private DateTime control;

    public CDateTimeCellEditorSimple(Composite parent, int style) {
        super(parent, SWT.NONE);
        this.style = style;
    }

    @Override
    protected Control createControl(Composite parent) {
        control = new DateTime(parent, style);
        return control;
    }

    @Override
    protected Object doGetValue() {
        return getDate(control);
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
            setDate(control, (Date) value);
        }
    }

    private void setDate(DateTime control, Date date) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        if ((control.getStyle() & SWT.TIME) != 0) {
            control.setTime(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE), c.get(java.util.Calendar.SECOND));
        } else {
            control.setDate(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH));
        }
    }

    private Date getDate(DateTime control) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        if ((control.getStyle() & SWT.TIME) != 0) {
            c.set(java.util.Calendar.HOUR_OF_DAY, control.getHours());
            c.set(java.util.Calendar.MINUTE, control.getMinutes());
            c.set(java.util.Calendar.SECOND, control.getSeconds());
        } else {
            c.set(java.util.Calendar.YEAR, control.getYear());
            c.set(java.util.Calendar.MONTH, control.getMonth());
            c.set(java.util.Calendar.DAY_OF_MONTH, control.getDay());
            c.set(java.util.Calendar.HOUR_OF_DAY, 0);
            c.set(java.util.Calendar.MINUTE, 0);
            c.set(java.util.Calendar.SECOND, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
        }
        return c.getTime();
    }
}