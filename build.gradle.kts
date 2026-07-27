plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "de.darkatra"
version = "0.1.0"

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

publishing {
    repositories {
        maven {
            name = "github-packages"
            url = uri("https://maven.pkg.github.com/DarkAtra/femtojar-gradle")
            credentials {
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
