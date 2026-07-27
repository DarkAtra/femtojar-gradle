package de.darkatra.femtojar;

import org.gradle.api.Named;
import org.gradle.api.tasks.Input;

public class JarEntry implements Named {

    private String name;
    private String in;
    private String out;
    private String compressionMode;
    private Boolean bundleResources;

    public JarEntry(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getCompressionMode() {
        return compressionMode;
    }

    public void setCompressionMode(String compressionMode) {
        this.compressionMode = compressionMode;
    }

    public Boolean getBundleResources() {
        return bundleResources;
    }

    public void setBundleResources(Boolean bundleResources) {
        this.bundleResources = bundleResources;
    }
}