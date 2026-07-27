package de.darkatra.femtojar;

import me.bechberger.femtojar.CompressionMode;
import me.bechberger.femtojar.JarReencoder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CacheableTask
public class ReencodeJarsTask extends DefaultTask {

    private final Property<Boolean> skip;
    private final Property<String> compressionMode;
    private final Property<Boolean> bundleResources;

    public ReencodeJarsTask() {
        this.skip = getProject().getObjects().property(Boolean.class);
        this.compressionMode = getProject().getObjects().property(String.class);
        this.bundleResources = getProject().getObjects().property(Boolean.class);
    }

    @Input
    public Property<Boolean> getSkip() {
        return skip;
    }

    @Input
    public Property<String> getCompressionMode() {
        return compressionMode;
    }

    @Input
    public Property<Boolean> getBundleResources() {
        return bundleResources;
    }

    @TaskAction
    public void reencodeJars() {
        if (skip.get()) {
            getLogger().lifecycle("Skipping femtojar execution");
            return;
        }

        // Get the extension to access jars configuration
        FemtojarExtension extension = getProject().getExtensions().getByType(FemtojarExtension.class);

        // Process each jar entry
        for (JarEntry entry : extension.getJars()) {
            try {
                reencodeSingleJar(entry);
            } catch (IOException e) {
                throw new GradleException("Failed to re-encode JAR: " + e.getMessage(), e);
            }
        }
    }

    private void reencodeSingleJar(JarEntry entry) throws IOException {
        // Resolve input and output paths
        Path inputPath = resolvePath(entry.getIn());
        Path outputPath = entry.getOut() != null ? resolvePath(entry.getOut()) : inputPath;

        // Validate input file exists
        if (!Files.exists(inputPath)) {
            throw new GradleException("Input JAR does not exist: " + inputPath);
        }

        // Parse compression mode
        CompressionMode compression = parseCompressionMode(entry.getCompressionMode());
        boolean bundleResources = entry.getBundleResources() != null ?
            entry.getBundleResources() : getBundleResources().get();

        try {
            // Create JarReencoder instance and reencode
            JarReencoder reencoder = new JarReencoder();

            // Determine if we're doing in-place or out-of-place reencoding
            boolean inPlace = inputPath.equals(outputPath);

            JarReencoder.ReencodeOptions reencodeOptions = new JarReencoder.ReencodeOptions(
                compression.useZopfli(),
                compression.zopfliIterations(),
                bundleResources,
                "gradle-plugin",
                false,
                null
            );
            if (inPlace) {
                // In-place reencoding
                reencoder.reencodeInPlaceBundled(inputPath, reencodeOptions);
            } else {
                // Out-of-place reencoding
                rewriteJarBundled(reencoder, inputPath, outputPath, reencodeOptions);
            }

            getLogger().lifecycle("JAR re-encoding completed for: " + inputPath);
        } catch (Exception e) {
            throw new GradleException("Failed to re-encode JAR: " + e.getMessage(), e);
        }
    }

    private void rewriteJarBundled(
        JarReencoder reencoder,
        Path inputPath,
        Path outputPath,
        JarReencoder.ReencodeOptions reencodeOptions
    ) throws Exception {

        Method rewriteJarBundled = JarReencoder.class.getDeclaredMethod(
            "rewriteJarBundled",
            Path.class,
            Path.class,
            JarReencoder.ReencodeOptions.class
        );
        rewriteJarBundled.setAccessible(true);

        try {
            rewriteJarBundled.invoke(reencoder, inputPath, outputPath, reencodeOptions);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof Exception) {
                throw (Exception) targetException;
            }
            throw e;
        }
    }

    private CompressionMode parseCompressionMode(String modeString) {
        if (modeString == null || modeString.isEmpty()) {
            return CompressionMode.DEFAULT;
        }

        try {
            return CompressionMode.parse(modeString);
        } catch (IllegalArgumentException e) {
            throw new GradleException("Invalid compression mode: " + modeString, e);
        }
    }

    private Path resolvePath(String path) {
        if (path == null) {
            throw new GradleException("JAR path must not be null");
        }

        Path resolvedPath = Paths.get(path);
        if (resolvedPath.isAbsolute()) {
            return resolvedPath;
        } else {
            // Resolve relative to project directory
            return getProject().getProjectDir().toPath().resolve(resolvedPath).normalize();
        }
    }
}
