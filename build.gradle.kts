import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    alias(libs.plugins.gradle.maven.publish)
}

group = "de.darkatra"
version = "dev-SNAPSHOT"

java {
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.femtojar.core)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertj.core)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("femtojar") {
            id = "de.darkatra.femtojar"
            implementationClass = "de.darkatra.femtojar.FemtojarGradlePlugin"
        }
    }
}

val releaseVersion = providers.environmentVariable("RELEASE_VERSION")
if (releaseVersion.isPresent) {
    mavenPublishing {
        coordinates(
            groupId = project.group.toString(),
            artifactId = project.name,
            version = releaseVersion.get()
        )

        pom {
            name.set(project.name)
            description.set("A Gradle plugin for parttimenerd/femtojar.")
            url.set("https://github.com/DarkAtra/femtojar-gradle")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/license/mit")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("DarkAtra")
                    name.set("Tommy Schmidt")
                    email.set("darkatra@gmail.com")
                    url.set("https://github.com/DarkAtra")
                    roles.add("maintainer")
                }
            }

            scm {
                connection.set("scm:git:https://github.com/DarkAtra/femtojar-gradle.git")
                developerConnection.set("scm:git:ssh://git@github.com/DarkAtra/femtojar-gradle.git")
                url.set("https://github.com/DarkAtra/femtojar-gradle")
            }
        }

        signAllPublications()
        publishToMavenCentral(automaticRelease = true)
    }
}
