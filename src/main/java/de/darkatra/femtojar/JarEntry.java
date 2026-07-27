package de.darkatra.femtojar;

import org.gradle.api.Named;
import org.jspecify.annotations.Nullable;

public class JarEntry implements Named {

    private String name;
    @Nullable
    private String in;
    @Nullable
    private String out;
    @Nullable
    private String compressionMode;
    @Nullable
    private Boolean bundleResources;

    public JarEntry(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Nullable
    public String getIn() {
        return in;
    }

    public void setIn(@Nullable final String in) {
        this.in = in;
    }

    @Nullable
    public String getOut() {
        return out;
    }

    public void setOut(@Nullable final String out) {
        this.out = out;
    }

    @Nullable
    public String getCompressionMode() {
        return compressionMode;
    }

    public void setCompressionMode(@Nullable final String compressionMode) {
        this.compressionMode = compressionMode;
    }

    @Nullable
    public Boolean getBundleResources() {
        return bundleResources;
    }

    public void setBundleResources(@Nullable final Boolean bundleResources) {
        this.bundleResources = bundleResources;
    }
}
