package de.darkatra.femtojar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class FemtojarGradlePluginTest {

    @Test
    public void pluginAppliesSuccessfully() {

        Project project = ProjectBuilder.builder().build();

        // Apply the plugin
        project.getPlugins().apply("de.darkatra.femtojar");

        // Verify extension is created
        assertNotNull(project.getExtensions().getByType(FemtojarExtension.class));

        // Verify task is registered
        assertNotNull(project.getTasks().findByName("reencodeJars"));
    }

    @Test
    public void pluginConfigurationWorks() {

        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("de.darkatra.femtojar");

        FemtojarExtension extension = project.getExtensions().getByType(FemtojarExtension.class);

        // Test default values
        assertFalse(extension.getSkip().get());
        assertEquals("DEFAULT", extension.getCompressionMode().get());
        assertTrue(extension.getBundleResources().get());
    }
}
