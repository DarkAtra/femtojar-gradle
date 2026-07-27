package de.darkatra.femtojar

import org.gradle.api.Named

open class JarEntry(
    var name: String,
    var `in`: String? = null,
    var out: String? = null,
    var compressionMode: String? = null,
    var bundleResources: Boolean? = null,
) : Named {

    override fun getName(): String = name
}
