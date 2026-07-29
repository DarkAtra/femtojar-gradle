package de.darkatra.femtojar

import me.bechberger.femtojar.rt.BundleBootstrap
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

open class FemtojarExtension @Inject constructor(
    objects: ObjectFactory,
    private val layout: ProjectLayout,
    providers: ProviderFactory,
) {

    internal val inputFile: RegularFileProperty = objects.fileProperty()
    internal val outputFile: RegularFileProperty = objects.fileProperty()

    var `in`: String? = null
        set(value) {
            field = value
            if (value == null) {
                inputFile.unset()
            } else {
                inputFile.set(layout.projectDirectory.file(value))
            }
        }

    var out: String? = null
        set(value) {
            field = value
            if (value == null) {
                outputFile.unset()
            } else {
                outputFile.set(layout.projectDirectory.file(value))
            }
        }

    val skip: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val compressionMode: Property<String> = objects.property(String::class.java).convention("DEFAULT")
    val bundleResources: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    val originalMainClass: Provider<String> = providers.of(MainClassValueSource::class.java) { spec ->
        spec.parameters.inputFile.set(inputFile)
    }
    val bootstrapMainClass: Provider<String> = providers.provider {
        BundleBootstrap::class.qualifiedName!!
    }
}
