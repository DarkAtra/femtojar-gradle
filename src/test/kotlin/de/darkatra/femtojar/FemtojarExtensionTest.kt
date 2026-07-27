package de.darkatra.femtojar

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class FemtojarExtensionTest {

    @Test
    fun defaultsAreApplied() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)

        assertThat(extension.skip.get()).isFalse()
        assertThat(extension.compressionMode.get()).isEqualTo("DEFAULT")
        assertThat(extension.bundleResources.get()).isTrue()
        assertThat(extension.`in`).isNull()
        assertThat(extension.out).isNull()
        assertThat(extension.mainClass.orNull).isNull()
    }

    @Test
    fun inputAndOutputPathsAreConfiguredDirectly() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)

        extension.`in` = "input.jar"
        extension.out = "build/output.jar"

        assertThat(extension.`in`).isEqualTo("input.jar")
        assertThat(extension.out).isEqualTo("build/output.jar")
    }

    @Test
    fun mainClassIsAbsentWhenInputDoesNotExist() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)
        extension.`in` = "missing.jar"

        assertThat(extension.mainClass.orNull).isNull()
    }

    @Test
    fun mainClassIsAbsentWhenInputHasNoManifest() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)
        createJar(project.projectDir.toPath().resolve("input.jar"))
        extension.`in` = "input.jar"

        assertThat(extension.mainClass.orNull).isNull()
    }

    @Test
    fun mainClassIsAbsentWhenManifestHasNoMainClass() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        }
        createJar(project.projectDir.toPath().resolve("input.jar"), manifest)
        extension.`in` = "input.jar"

        assertThat(extension.mainClass.orNull).isNull()
    }

    @Test
    fun mainClassIsReadFromTheInputManifest() {

        val project = ProjectBuilder.builder().build()
        val extension = project.objects.newInstance(FemtojarExtension::class.java)
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = "example.Main"
        }
        createJar(project.projectDir.toPath().resolve("input.jar"), manifest)
        extension.`in` = "input.jar"

        assertThat(extension.mainClass.get()).isEqualTo("example.Main")
    }

    private fun createJar(path: java.nio.file.Path, manifest: Manifest? = null) {

        val output = Files.newOutputStream(path)
        val jar = if (manifest == null) JarOutputStream(output) else JarOutputStream(output, manifest)
        jar.use { }
    }
}
