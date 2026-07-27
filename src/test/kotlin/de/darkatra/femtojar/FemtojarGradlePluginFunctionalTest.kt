package de.darkatra.femtojar

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class FemtojarGradlePluginFunctionalTest {

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun reencodeTaskReusesConfigurationAndBuildCaches() {

        Files.writeString(
            projectDir.resolve("settings.gradle.kts"),
            "rootProject.name = \"cache-test\"\n",
        )
        Files.writeString(
            projectDir.resolve("build.gradle.kts"),
            """
            plugins {
                id("de.darkatra.femtojar")
            }

            femtojar {
                `in` = "input.jar"
                out = "build/output.jar"
            }

            check(femtojar.mainClass.get() == "example.Main")
            """.trimIndent(),
        )

        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = "example.Main"
        }
        JarOutputStream(Files.newOutputStream(projectDir.resolve("input.jar")), manifest).use { }
        val output = projectDir.resolve("build/output.jar")

        val firstRun = runGradle()

        assertThat(firstRun.task(":reencodeJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(output).exists()

        Files.delete(output)

        val secondRun = runGradle()

        assertThat(secondRun.output).contains("Reusing configuration cache.")
        assertThat(secondRun.task(":reencodeJar")?.outcome).isEqualTo(TaskOutcome.FROM_CACHE)
        assertThat(output).exists()
    }

    private fun runGradle() = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withPluginClasspath()
        .withArguments("reencodeJar", "--build-cache", "--configuration-cache", "--stacktrace")
        .build()
}
