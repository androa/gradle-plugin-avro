# Avro Gradle Plugin

A Gradle plugin that simplifies code generation from Apache Avro schemas, protocols, and IDL files.

## Features

- Generates Java classes from Avro schema files (`.avsc`)
- Generates Java interfaces from Avro protocol files (`.avpr`)
- Automatically transforms Avro IDL files (`.avdl`) to protocols and generates code
- Configurable code generation options
- Gradle cacheable tasks for improved build performance
- Automatic integration with Java and Kotlin source sets

## Installation

```kotlin
plugins {
    id("io.github.androa.gradle.plugin.avro") version "0.1.0"
}
```

## Usage

### Zero Configuration

Simply add the plugin and it will compile any Avro schema files found in the default directory (`src/main/avro`) and
place the generated sources in `build/generated-avro` which is included in the Gradle Build.

### Basic Configuration

```kotlin
generateAvro {
    schemasDir.set(layout.projectDirectory.dir("src/main/avro"))
    outputDir.set(layout.buildDirectory.dir("generated-avro"))
}
```

### Advanced Configuration

```kotlin
import org.apache.avro.compiler.specific.SpecificCompiler

generateAvro {
    // Input/output directories
    schemasDir.set(layout.projectDirectory.dir("src/main/avro"))
    outputDir.set(layout.buildDirectory.dir("generated-avro"))

    // Code generation options
    encoding = "UTF-8"
    fieldVisibility = SpecificCompiler.FieldVisibility.PRIVATE
    noSetters = true
    addNullSafeAnnotations.set(true)
    stringType = true
    addExtraOptionalGetters = false
    useBigDecimal = true
}
```

## Supported File Types

- **Schema files** (`.avsc`): JSON Avro schema definitions
- **Protocol files** (`.avpr`): JSON Avro protocol definitions
- **IDL files** (`.avdl`): Avro IDL format (automatically converted to protocols)

## Configuration Options

| Option                    | Description                                                      | Default                |
|---------------------------|------------------------------------------------------------------|------------------------|
| `schemasDir`              | Directory containing Avro schema files                           | `src/main/avro`        |
| `outputDir`               | Directory where generated sources will be placed                 | `build/generated-avro` |
| `encoding`                | Character encoding for generated files                           | System default         |
| `stringType`              | Use Java String instead of Avro's Utf8                           | `false`                |
| `fieldVisibility`         | Visibility of generated fields                                   | `PRIVATE`              |
| `noSetters`               | Don't generate setter methods                                    | `false`                |
| `addNullSafeAnnotations`  | Add `@Nullable` and `@NonNull` annotations                       | `false`                |
| `addExtraOptionalGetters` | Generate additional getters with format `getOptional<FieldName>` | `false`                |
| `createOptionalGetters`   | Generate getters returning `Optional<T>`                         | None                   |
| `useBigDecimal`           | Use `java.math.BigDecimal` for decimal type                      | `false`                |

## Task

The plugin adds a `generateAvro` task that automatically runs before compilation.