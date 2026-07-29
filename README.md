# femtojar-gradle

A simplified Gradle Plugin for
[femtojar](https://github.com/parttimenerd/femtojar). It adds a Gradle task for re-encoding executable JARs with Femtojar's custom class loader and compression.

> Femtojar is intended for executable, shaded, or uber JARs rather than library
> JARs. See the upstream project for details about its format and limitations.

## Requirements

- Java 17 or newer
- An executable JAR to re-encode

## Usage

Apply the plugin:

```kotlin
plugins {
    id("de.darkatra.femtojar") version "0.1.0"
}
```

Configure the input and output JARs:

```kotlin
femtojar {
    `in` = "build/libs/application.jar"
    out = "build/libs/application-femto.jar"

    compressionMode = "DEFAULT" // this is the default
    bundleResources = true // this is the default
    skip = false // this is the default
}
```

Paths are resolved relative to the project directory. The input and output must be different paths, and the input JAR must exist when the task runs.

Re-encode the JAR with:

```shell
./gradlew reencodeJar
```

## Configuration

| Property             | Type               | Default                                       | Description                                                                                                |
|----------------------|--------------------|-----------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `in`                 | `String?`          | unset                                         | Path to the executable input JAR.                                                                          |
| `out`                | `String?`          | unset                                         | Path for the re-encoded output JAR.                                                                        |
| `compressionMode`    | `String`           | `DEFAULT`                                     | Femtojar compression mode.                                                                                 |
| `bundleResources`    | `Boolean`          | `true`                                        | Bundle non-`META-INF` resources into the compressed blob.                                                  |
| `skip`               | `Boolean`          | `false`                                       | Skip re-encoding when the task executes.                                                                   |
| `originalMainClass`  | `Provider<String>` | absent                                        | Read-only `Main-Class` value from the input JAR manifest.                                                  |
| `bootstrapMainClass` | `Provider<String>` | `"me.bechberger.femtojar.rt.BundleBootstrap"` | Read-only `Main-Class` of the custom classloader. This one should be used when running the compressed jar. |

Both `in` and `out` are required unless `skip` is `true`. Invalid compression modes fail the task.

## Building

Run the test suite with:

```shell
./gradlew test
```

Build the plugin with:

```shell
./gradlew build
```

## Upstream project

The JAR transformation is provided by
[parttimenerd/femtojar](https://github.com/parttimenerd/femtojar). Refer to its documentation for compression details, runtime caveats, and recommendations for
testing generated JARs.
