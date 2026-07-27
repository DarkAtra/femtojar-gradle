package de.darkatra.femtojar

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.InvalidUserDataException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class FemtojarExtensionTest {

    @Test
    fun defaultsAreApplied() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)

        assertThat(extension.skip.get()).isFalse()
        assertThat(extension.compressionMode.get()).isEqualTo("DEFAULT")
        assertThat(extension.bundleResources.get()).isTrue()
        assertThat(extension.jars).isEmpty()
    }

    @Test
    fun jarsContainerCreatesNamedEntriesWithoutDefaults() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)

        val entry = extension.jars.create("app")
        entry.`in` = "input.jar"

        assertThat(entry.name).isEqualTo("app")
        assertThat(entry.`in`).isEqualTo("input.jar")
        assertThat(entry.out).isNull()
        assertThat(entry.compressionMode).isNull()
        assertThat(entry.bundleResources).isNull()
        assertThat(extension.jars.getByName("app")).isSameAs(entry)
    }

    @Test
    fun jarsContainerRejectsDuplicateNames() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)

        extension.jars.create("app")

        assertThatThrownBy { extension.jars.create("app") }
            .isInstanceOf(InvalidUserDataException::class.java)
    }
}
