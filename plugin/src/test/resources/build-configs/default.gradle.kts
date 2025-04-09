import org.apache.avro.compiler.specific.SpecificCompiler

plugins {
    id("io.github.androa.gradle.plugin.avro")
    kotlin("jvm") version "2.1.20"
}

generateAvro {
    noSetters = true
    // Both assign and set() is possible
    addNullSafeAnnotations.set(true)
    encoding = "UTF-8"
    fieldVisibility = SpecificCompiler.FieldVisibility.PRIVATE

    // schemas.from(project.fileTree("src/main/avro"))
    // outputDir.set(layout.buildDirectory.dir("generated-avro"))
}
