package de.darkatra.femtojar

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.util.jar.Attributes
import java.util.jar.JarFile

internal abstract class MainClassValueSource : ValueSource<String, MainClassValueSource.Parameters> {

    interface Parameters : ValueSourceParameters {

        val inputFile: RegularFileProperty
    }

    override fun obtain(): String? {

        val inputFile = parameters.inputFile.orNull?.asFile ?: return null
        if (!inputFile.isFile) {
            return null
        }

        return JarFile(inputFile).use { jar ->
            jar.manifest?.mainAttributes?.getValue(Attributes.Name.MAIN_CLASS)
        }
    }
}