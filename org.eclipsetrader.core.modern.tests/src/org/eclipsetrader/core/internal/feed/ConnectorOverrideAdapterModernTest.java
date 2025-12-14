package org.eclipsetrader.core.internal.feed;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;

@RunWith(JUnitPlatform.class)
public class ConnectorOverrideAdapterModernTest {

    private final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private final File file = new File("overrides.xml");

    @AfterEach
    void tearDown() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testLoadMissingFile() throws Exception {
        if (file.exists()) {
            file.delete();
        }
        ConnectorOverrideAdapter adapter = new ConnectorOverrideAdapter(file);
        File out = new File("out.xml");
        adapter.save(out);
        String content = new String(java.nio.file.Files.readAllBytes(out.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("<list/>") || content.contains("<list></list>"));
        if (out.exists()) out.delete();
    }

    @Test
    void testLoadEmptyFile() throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.write(header + "<list/>\n");
        writer.close();
        ConnectorOverrideAdapter adapter = new ConnectorOverrideAdapter(file);
        File out = new File("out.xml");
        adapter.save(out);
        String content = new String(java.nio.file.Files.readAllBytes(out.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("<list/>") || content.contains("<list></list>"));
        if (out.exists()) out.delete();
    }

    @Test
    void testLoadFile() throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.write(header + "<list><override security=\"local:securities#1\"/></list>\n");
        writer.close();
        ConnectorOverrideAdapter adapter = new ConnectorOverrideAdapter(file);
        File out = new File("out.xml");
        adapter.save(out);
        String content = new String(java.nio.file.Files.readAllBytes(out.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("<list"));
        if (out.exists()) out.delete();
    }
}
