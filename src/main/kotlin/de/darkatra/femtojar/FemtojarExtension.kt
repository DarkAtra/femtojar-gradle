package de.darkatra.femtojar

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class FemtojarExtension @Inject constructor(objects: ObjectFactory) {

    val skip: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val compressionMode: Property<String> = objects.property(String::class.java).convention("DEFAULT")
    val bundleResources: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val jars: NamedDomainObjectContainer<JarEntry> = objects.domainObjectContainer(JarEntry::class.java)
}
