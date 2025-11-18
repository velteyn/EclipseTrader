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

package org.eclipsetrader.ui.internal.markets;

import java.util.Calendar;

import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.ui.Util;

public class HolidayDialog extends Dialog {

    DateTime date;
    Text description;
    Button closed;
    Button open;
    Label openTimeLabel;
    DateTime openTime;
    Label closeTimeLabel;
    DateTime closeTime;
    MarketHolidayElement element;

    private SelectionAdapter buttonSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            updateButtonsEnablement();
        }
    };

    private ModifyListener validationModifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            getButton(IDialogConstants.OK_ID).setEnabled(isValid());
        }
    };

    private SelectionAdapter validationSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            getButton(IDialogConstants.OK_ID).setEnabled(isValid());
        }
    };

    public HolidayDialog(Shell parentShell, MarketHolidayElement day) {
        super(parentShell);
        this.element = day;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Edit Market Holiday");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);

        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        Label label = new Label(content, SWT.NONE);
        label.setText("Date");
        date = new DateTime(content, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);

        label = new Label(content, SWT.NONE);
        label.setText("Description");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(50), SWT.DEFAULT));
        description = new Text(content, SWT.BORDER);
        description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) description.getLayoutData()).widthHint = convertWidthInCharsToPixels(50);

        label = new Label(content, SWT.NONE);
        closed = new Button(content, SWT.RADIO);
        closed.setText("Market is closed");

        label = new Label(content, SWT.NONE);
        open = new Button(content, SWT.RADIO);
        open.setText("Market is partially open");

        openTimeLabel = new Label(content, SWT.NONE);
        openTimeLabel.setText("Open Time");
        openTime = new DateTime(content, SWT.BORDER | SWT.TIME | SWT.SHORT);
        openTime.setLayoutData(new GridData(convertHorizontalDLUsToPixels(50), SWT.DEFAULT));

        closeTimeLabel = new Label(content, SWT.NONE);
        closeTimeLabel.setText("Close Time");
        closeTime = new DateTime(content, SWT.BORDER | SWT.TIME | SWT.SHORT);
        closeTime.setLayoutData(new GridData(convertHorizontalDLUsToPixels(50), SWT.DEFAULT));

        if (element != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(element.getDate());
            date.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            description.setText(element.getDescription() != null ? element.getDescription() : "");
            closed.setSelection(element.getOpenTime() == null || element.getCloseTime() == null);
            open.setSelection(element.getOpenTime() != null && element.getCloseTime() != null);
            if (element.getOpenTime() != null) {
                calendar.setTime(element.getOpenTime());
                openTime.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            }
            if (element.getCloseTime() != null) {
                calendar.setTime(element.getCloseTime());
                closeTime.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            }
        }
        else {
            Calendar calendar = Calendar.getInstance();
            date.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            description.setText("");
            closed.setSelection(true);
            open.setSelection(false);
        }

        date.addSelectionListener(validationSelectionListener);
        description.addModifyListener(validationModifyListener);
        closed.addSelectionListener(buttonSelectionListener);
        open.addSelectionListener(buttonSelectionListener);
        openTime.addSelectionListener(validationSelectionListener);
        closeTime.addSelectionListener(validationSelectionListener);

        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        updateButtonsEnablement();
        return control;
    }

    protected void updateButtonsEnablement() {
        openTimeLabel.setEnabled(open.getSelection());
        openTime.setEnabled(open.getSelection());
        closeTimeLabel.setEnabled(open.getSelection());
        closeTime.setEnabled(open.getSelection());
        getButton(IDialogConstants.OK_ID).setEnabled(isValid());
    }

    protected boolean isValid() {
        if (description.getText().equals("")) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        commitChanges();
        super.okPressed();
    }

    protected void commitChanges() {
        if (element == null) {
            element = new MarketHolidayElement();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, date.getDay());
        element.setDate(calendar.getTime());

        element.setDescription(!description.getText().equals("") ? description.getText() : null);

        if (open.getSelection()) {
            calendar.set(Calendar.HOUR_OF_DAY, openTime.getHours());
            calendar.set(Calendar.MINUTE, openTime.getMinutes());
            calendar.set(Calendar.SECOND, openTime.getSeconds());
            element.setOpenTime(calendar.getTime());

            calendar.set(Calendar.HOUR_OF_DAY, closeTime.getHours());
            calendar.set(Calendar.MINUTE, closeTime.getMinutes());
            calendar.set(Calendar.SECOND, closeTime.getSeconds());
            element.setCloseTime(calendar.getTime());
        }
        else {
            element.setOpenTime(null);
            element.setCloseTime(null);
        }
    }

    public MarketHolidayElement getElement() {
        return element;
    }
}
