package de.darkatra.femtojar

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused") // used via build.gradle.kts
class FemtojarGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create("femtojar", FemtojarExtension::class.java)

        project.tasks.register("reencodeJar", ReencodeJarTask::class.java) { task ->
            task.group = "femtojar"
            task.description = "Re-encode JAR files with custom class loader and compression."

            task.skip.set(extension.skip)
            task.compressionMode.set(extension.compressionMode)
            task.bundleResources.set(extension.bundleResources)
            task.inputFile.set(extension.inputFile)
            task.outputFile.set(extension.outputFile)
        }
    }
}
