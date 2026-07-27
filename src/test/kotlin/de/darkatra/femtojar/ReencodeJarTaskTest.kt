package de.darkatra.femtojar

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ReencodeJarTaskTest {

    @Test
    fun skipShortCircuitsExecutionWithoutConfiguredPaths() {

        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("reencodeJar", ReencodeJarTask::class.java).get()
        task.skip.set(true)
        task.compressionMode.set("DEFAULT")
        task.bundleResources.set(true)

        task.reencodeJar()
    }

    @Test
    fun missingInPathThrowsDescriptiveException() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")

        val task = project.tasks.getByName("reencodeJar") as ReencodeJarTask

        assertThatThrownBy { task.reencodeJar() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("Input JAR path must not be null")
    }

    @Test
    fun missingInputJarThrowsDescriptiveException() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")
        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.`in` = "does-not-exist.jar"
        extension.out = "output.jar"

        val task = project.tasks.getByName("reencodeJar") as ReencodeJarTask

        assertThatThrownBy { task.reencodeJar() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("Input JAR does not exist")
            .hasMessageContaining("does-not-exist.jar")
    }

    @Test
    fun relativeInputPathIsResolvedAgainstTheProjectDirectory() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")
        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.`in` = "relative/does-not-exist.jar"
        extension.out = "output.jar"

        val task = project.tasks.getByName("reencodeJar") as ReencodeJarTask

        val expectedPath = project.projectDir.toPath().resolve("relative/does-not-exist.jar").normalize()

        assertThatThrownBy { task.reencodeJar() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining(expectedPath.toString())
    }

    @Test
    fun invalidCompressionModeThrowsDescriptiveException() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")

        // create a real, existing input file so validation gets past the path check
        val inputFile = project.projectDir.toPath().resolve("input.jar")
        Files.createFile(inputFile)

        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.`in` = "input.jar"
        extension.out = "output.jar"
        extension.compressionMode.set("not-a-real-mode")

        val task = project.tasks.getByName("reencodeJar") as ReencodeJarTask

        assertThatThrownBy { task.reencodeJar() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("Invalid compression mode")
            .hasMessageContaining("not-a-real-mode")
    }

    @Test
    fun missingOutPathThrowsDescriptiveException() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")
        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.`in` = "input.jar"

        val task = project.tasks.getByName("reencodeJar") as ReencodeJarTask

        assertThatThrownBy { task.reencodeJar() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("Output JAR path must not be null")
    }

    @Test
    fun matchingInputAndOutputPathsAreRejected() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")
        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.`in` = "build/../input.jar"
        extension.out = "input.jar"

        val task = project.tasks.getByName("reencodeJar") as ReencodeJarTask

        assertThatThrownBy { task.reencodeJar() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("Input and output JAR paths must be different")
    }
}
