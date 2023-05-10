package org.eclipsetrader.jessx.internal.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.internal.core.connector.SnapshotConnector;
import org.eclipsetrader.jessx.internal.core.connector.StreamingConnector;



public class AuthenticationPage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Combo driver;
	
    public AuthenticationPage() {
    }


	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(JessxActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        Label label = new Label(content, SWT.NONE);
        label.setText("Driver");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
        driver = new Combo(content, SWT.READ_ONLY);
        driver.add("Streaming");
        driver.add("Snapshot"); //TODO togliere perche non abbiamo gli snapshot (per ora)
        driver.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        performDefaults();

        return content;
	}
	

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        performApply();
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        switch (driver.getSelectionIndex()) {
            case 0:
                getPreferenceStore().setValue(JessxActivator.PREFS_DRIVER, StreamingConnector.class.getName());
                break;
            case 1:
                getPreferenceStore().setValue(JessxActivator.PREFS_DRIVER, SnapshotConnector.class.getName());
                break;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        driver.select(getDriverIndex());
    }

    protected int getDriverIndex() {
        String driver = getPreferenceStore().getString(JessxActivator.PREFS_DRIVER);
        if (driver.equals(SnapshotConnector.class.getName())) {
            return 1;
        }
        return 0;
    }

}
