package de.darkatra.femtojar

import me.bechberger.femtojar.CompressionMode
import me.bechberger.femtojar.JarReencoder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.lang.reflect.InvocationTargetException
import java.nio.file.Files
import java.nio.file.Path

@CacheableTask
abstract class ReencodeJarTask : DefaultTask() {

    @get:Input
    abstract val skip: Property<Boolean>

    @get:Input
    abstract val compressionMode: Property<String>

    @get:Input
    abstract val bundleResources: Property<Boolean>

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun reencodeJar() {

        if (skip.get()) {
            logger.lifecycle("Skipping femtojar execution")
            return
        }

        val inputPath = inputFile.orNull?.asFile?.toPath()
            ?: throw GradleException("Input JAR path must not be null")
        val outputPath = outputFile.orNull?.asFile?.toPath()
            ?: throw GradleException("Output JAR path must not be null")

        if (inputPath.toAbsolutePath().normalize() == outputPath.toAbsolutePath().normalize()) {
            throw GradleException("Input and output JAR paths must be different: $inputPath")
        }

        if (!Files.exists(inputPath)) {
            throw GradleException("Input JAR does not exist: $inputPath")
        }

        val compression = parseCompressionMode(compressionMode.orNull)

        try {
            val reencoder = JarReencoder()

            val reencodeOptions = JarReencoder.ReencodeOptions(
                compression.useZopfli(),
                compression.zopfliIterations(),
                bundleResources.get(),
                "gradle-plugin",
                false,
                null
            )

            rewriteJarBundled(reencoder, inputPath, outputPath, reencodeOptions)

            logger.lifecycle("JAR re-encoding completed for: $inputPath")
        } catch (e: Exception) {
            throw GradleException("Failed to re-encode JAR: ${e.message}", e)
        }
    }

    private fun rewriteJarBundled(
        reencoder: JarReencoder,
        inputPath: Path,
        outputPath: Path,
        reencodeOptions: JarReencoder.ReencodeOptions
    ) {

        val rewriteJarBundled = JarReencoder::class.java.getDeclaredMethod(
            "rewriteJarBundled",
            Path::class.java,
            Path::class.java,
            JarReencoder.ReencodeOptions::class.java
        )
        rewriteJarBundled.isAccessible = true

        try {
            rewriteJarBundled.invoke(reencoder, inputPath, outputPath, reencodeOptions)
        } catch (e: InvocationTargetException) {
            throw (e.targetException as? Exception) ?: e
        }
    }

    private fun parseCompressionMode(modeString: String?): CompressionMode {

        if (modeString.isNullOrEmpty()) {
            return CompressionMode.DEFAULT
        }

        return try {
            CompressionMode.parse(modeString)
        } catch (e: IllegalArgumentException) {
            throw GradleException("Invalid compression mode: $modeString", e)
        }
    }
}
