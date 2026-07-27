package de.darkatra.femtojar

import me.bechberger.femtojar.CompressionMode
import me.bechberger.femtojar.JarReencoder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.lang.reflect.InvocationTargetException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@CacheableTask
open class ReencodeJarsTask : DefaultTask() {

    @get:Input
    val skip: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    val compressionMode: Property<String> = project.objects.property(String::class.java)

    @get:Input
    val bundleResources: Property<Boolean> = project.objects.property(Boolean::class.java)

    @TaskAction
    fun reencodeJars() {

        if (skip.get()) {
            logger.lifecycle("Skipping femtojar execution")
            return
        }

        // Get the extension to access jars configuration
        val extension = project.extensions.getByType(FemtojarExtension::class.java)

        // Process each jar entry
        for (entry in extension.jars) {
            reencodeSingleJar(entry)
        }
    }

    private fun reencodeSingleJar(entry: JarEntry) {

        val inputPath = resolvePath(entry.`in`)
        val outputPath = entry.out?.let { resolvePath(it) } ?: inputPath

        if (!Files.exists(inputPath)) {
            throw GradleException("Input JAR does not exist: $inputPath")
        }

        val compression = parseCompressionMode(entry.compressionMode)
        val useBundleResources = entry.bundleResources ?: bundleResources.get()

        try {
            val reencoder = JarReencoder()

            val inPlace = inputPath == outputPath

            val reencodeOptions = JarReencoder.ReencodeOptions(
                compression.useZopfli(),
                compression.zopfliIterations(),
                useBundleResources,
                "gradle-plugin",
                false,
                null
            )

            if (inPlace) {
                reencoder.reencodeInPlaceBundled(inputPath, reencodeOptions)
            } else {
                rewriteJarBundled(reencoder, inputPath, outputPath, reencodeOptions)
            }

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

    private fun resolvePath(path: String?): Path {

        if (path == null) {
            throw GradleException("JAR path must not be null")
        }

        val resolvedPath = Paths.get(path)
        return if (resolvedPath.isAbsolute) {
            resolvedPath
        } else {
            project.projectDir.toPath().resolve(resolvedPath).normalize()
        }
    }
}
