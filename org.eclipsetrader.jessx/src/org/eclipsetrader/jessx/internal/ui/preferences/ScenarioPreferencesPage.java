package org.eclipsetrader.jessx.internal.ui.preferences;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.preferences.PreferenceConstants;

public class ScenarioPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private Combo scenarioCombo;
    private List<String> scenarios = new ArrayList<String>();
    private Label infoLabel;

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(JessxActivator.getDefault().getPreferenceStore());
        setDescription("Select the JessX experiment scenario file (.xml). Drop custom scenarios into the 'scenarios' folder to make them appear here.");
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(3, false));

        Label label = new Label(container, SWT.NONE);
        label.setText("Scenario");
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        scenarioCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        scenarioCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button browse = new Button(container, SWT.PUSH);
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
                fd.setFilterExtensions(new String[] { "*.xml" });
                String path = fd.open();
                if (path != null) {
                    if (!scenarios.contains(path)) {
                        scenarios.add(path);
                        scenarioCombo.add(path);
                    }
                    scenarioCombo.select(scenarioCombo.indexOf(path));
                }
            }
        });

        infoLabel = new Label(container, SWT.WRAP);
        infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
        infoLabel.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));

        loadScenarios();
        String current = getPreferenceStore().getString(PreferenceConstants.P_SCENARIO_FILE);
        if (current == null || current.isEmpty()) {
            current = "default.xml";
        }
        int idx = scenarioCombo.indexOf(current);
        if (idx >= 0) {
            scenarioCombo.select(idx);
        } else {
            scenarioCombo.add(current);
            scenarioCombo.select(scenarioCombo.indexOf(current));
        }

        updateInfo();
        return container;
    }

    private void loadScenarios() {
        // bundled scenarios
        Enumeration<?> en = JessxActivator.getDefault().getBundle().findEntries("resources/scenarios", "*.xml", false);
        if (en != null) {
            while (en.hasMoreElements()) {
                URL url = (URL) en.nextElement();
                String path = url.getPath();
                String name = path.substring(path.lastIndexOf('/') + 1);
                if (!scenarios.contains(name)) {
                    scenarios.add(name);
                    scenarioCombo.add(name);
                }
            }
        }

        // user scenarios under stateLocation/scenarios
        File dir = JessxActivator.getDefault().getStateLocation().append("scenarios").toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".xml"));
        if (files != null) {
            for (File f : files) {
                String abs = f.getAbsolutePath();
                if (!scenarios.contains(abs)) {
                    scenarios.add(abs);
                    scenarioCombo.add(abs);
                }
            }
        }

        // include default.xml from bundle
        URL defUrl = FileLocator.find(JessxActivator.getDefault().getBundle(), new Path("resources/default.xml"), null);
        if (defUrl != null && scenarioCombo.indexOf("default.xml") < 0) {
            scenarioCombo.add("default.xml");
        }
    }

    private void updateInfo() {
        File dir = JessxActivator.getDefault().getStateLocation().append("scenarios").toFile();
        infoLabel.setText("User scenarios folder: " + dir.getAbsolutePath());
    }

    @Override
    public boolean performOk() {
        String selected = scenarioCombo.getText();
        IPreferenceStore store = getPreferenceStore();
        store.setValue(PreferenceConstants.P_SCENARIO_FILE, selected);
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        scenarioCombo.setText("default.xml");
        super.performDefaults();
    }
}
