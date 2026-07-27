package de.darkatra.femtojar;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;
import java.util.List;

public class FemtojarExtension {

    private final Property<Boolean> skip;
    private final Property<String> compressionMode;
    private final Property<Boolean> bundleResources;
    private final NamedDomainObjectContainer<JarEntry> jars;

    @Inject
    public FemtojarExtension(ObjectFactory objects) {
        this.skip = objects.property(Boolean.class).convention(false);
        this.compressionMode = objects.property(String.class).convention("DEFAULT");
        this.bundleResources = objects.property(Boolean.class).convention(true);
        this.jars = objects.domainObjectContainer(JarEntry.class);
    }

    public Property<Boolean> getSkip() {
        return skip;
    }

    public Property<String> getCompressionMode() {
        return compressionMode;
    }

    public Property<Boolean> getBundleResources() {
        return bundleResources;
    }

    public NamedDomainObjectContainer<JarEntry> getJars() {
        return jars;
    }
}