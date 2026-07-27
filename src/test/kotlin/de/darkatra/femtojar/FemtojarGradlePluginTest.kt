package de.darkatra.femtojar

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FemtojarGradlePluginTest {

    @Test
    fun pluginAppliesSuccessfully() {

        val project = ProjectBuilder.builder().build()

        // Apply the plugin
        project.plugins.apply("de.darkatra.femtojar")

        // Verify extension is created
        assertNotNull(project.extensions.getByType(FemtojarExtension::class.java))

        // Verify task is registered
        assertNotNull(project.tasks.findByName("reencodeJar"))
    }

    @Test
    fun pluginConfigurationWorks() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")

        val extension = project.extensions.getByType(FemtojarExtension::class.java)

        // Test default values
        assertFalse(extension.skip.get())
        assertEquals("DEFAULT", extension.compressionMode.get())
        assertTrue(extension.bundleResources.get())
    }
}
