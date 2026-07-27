package de.darkatra.femtojar

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ReencodeJarsTaskTest {

    @Test
    fun skipShortCircuitsExecutionWithoutTouchingTheJarsContainer() {

        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("reencodeJars", ReencodeJarsTask::class.java).get()
        task.skip.set(true)
        task.compressionMode.set("DEFAULT")
        task.bundleResources.set(true)

        // the femtojar extension was never applied, so this would fail if the
        // jars container was accessed instead of returning early
        task.reencodeJars()
    }

    @Test
    fun missingInPathThrowsDescriptiveException() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")
        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.jars.create("app")

        val task = project.tasks.getByName("reencodeJars") as ReencodeJarsTask

        assertThatThrownBy { task.reencodeJars() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("JAR path must not be null")
    }

    @Test
    fun missingInputJarThrowsDescriptiveException() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")
        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.jars.create("app").`in` = "does-not-exist.jar"

        val task = project.tasks.getByName("reencodeJars") as ReencodeJarsTask

        assertThatThrownBy { task.reencodeJars() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("Input JAR does not exist")
            .hasMessageContaining("does-not-exist.jar")
    }

    @Test
    fun relativeInputPathIsResolvedAgainstTheProjectDirectory() {

        val project = ProjectBuilder.builder().build()
        project.plugins.apply("de.darkatra.femtojar")
        val extension = project.extensions.getByType(FemtojarExtension::class.java)
        extension.jars.create("app").`in` = "relative/does-not-exist.jar"

        val task = project.tasks.getByName("reencodeJars") as ReencodeJarsTask

        val expectedPath = project.projectDir.toPath().resolve("relative/does-not-exist.jar").normalize()

        assertThatThrownBy { task.reencodeJars() }
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
        val entry = extension.jars.create("app")
        entry.`in` = "input.jar"
        entry.compressionMode = "not-a-real-mode"

        val task = project.tasks.getByName("reencodeJars") as ReencodeJarsTask

        assertThatThrownBy { task.reencodeJars() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("Invalid compression mode")
            .hasMessageContaining("not-a-real-mode")
    }
}
