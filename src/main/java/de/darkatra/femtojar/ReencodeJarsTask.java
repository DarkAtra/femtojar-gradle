package de.darkatra.femtojar;

import me.bechberger.femtojar.CompressionMode;
import me.bechberger.femtojar.JarReencoder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.jspecify.annotations.Nullable;

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
        final FemtojarExtension extension = getProject().getExtensions().getByType(FemtojarExtension.class);

        // Process each jar entry
        for (final JarEntry entry : extension.getJars()) {
            try {
                reencodeSingleJar(entry);
            } catch (final IOException e) {
                throw new GradleException("Failed to re-encode JAR: " + e.getMessage(), e);
            }
        }
    }

    private void reencodeSingleJar(final JarEntry entry) throws IOException {

        final Path inputPath = resolvePath(entry.getIn());
        final Path outputPath = entry.getOut() != null ? resolvePath(entry.getOut()) : inputPath;

        if (!Files.exists(inputPath)) {
            throw new GradleException("Input JAR does not exist: " + inputPath);
        }

        final CompressionMode compression = parseCompressionMode(entry.getCompressionMode());
        final boolean bundleResources = entry.getBundleResources() != null ?
            entry.getBundleResources() : getBundleResources().get();

        try {
            final JarReencoder reencoder = new JarReencoder();

            final boolean inPlace = inputPath.equals(outputPath);

            final JarReencoder.ReencodeOptions reencodeOptions = new JarReencoder.ReencodeOptions(
                compression.useZopfli(),
                compression.zopfliIterations(),
                bundleResources,
                "gradle-plugin",
                false,
                null
            );

            if (inPlace) {
                reencoder.reencodeInPlaceBundled(inputPath, reencodeOptions);
            } else {
                rewriteJarBundled(reencoder, inputPath, outputPath, reencodeOptions);
            }

            getLogger().lifecycle("JAR re-encoding completed for: " + inputPath);
        } catch (final Exception e) {
            throw new GradleException("Failed to re-encode JAR: " + e.getMessage(), e);
        }
    }

    private void rewriteJarBundled(
        final JarReencoder reencoder,
        final Path inputPath,
        final Path outputPath,
        final JarReencoder.ReencodeOptions reencodeOptions
    ) throws Exception {

        final Method rewriteJarBundled = JarReencoder.class.getDeclaredMethod(
            "rewriteJarBundled",
            Path.class,
            Path.class,
            JarReencoder.ReencodeOptions.class
        );
        rewriteJarBundled.setAccessible(true);

        try {
            rewriteJarBundled.invoke(reencoder, inputPath, outputPath, reencodeOptions);
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof Exception) {
                throw (Exception) targetException;
            }
            throw e;
        }
    }

    private CompressionMode parseCompressionMode(@Nullable final String modeString) {

        if (modeString == null || modeString.isEmpty()) {
            return CompressionMode.DEFAULT;
        }

        try {
            return CompressionMode.parse(modeString);
        } catch (final IllegalArgumentException e) {
            throw new GradleException("Invalid compression mode: " + modeString, e);
        }
    }

    private Path resolvePath(@Nullable final String path) {

        if (path == null) {
            throw new GradleException("JAR path must not be null");
        }

        final Path resolvedPath = Paths.get(path);
        if (resolvedPath.isAbsolute()) {
            return resolvedPath;
        } else {
            return getProject().getProjectDir().toPath().resolve(resolvedPath).normalize();
        }
    }
}
