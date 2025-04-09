import org.apache.avro.compiler.specific.SpecificCompiler

plugins {
    id("io.github.androa.gradle.plugin.avro")
    kotlin("jvm") version "2.1.20"
}

generateAvro {
    noSetters = true
    addNullSafeAnnotations.set(true)
    encoding = "UTF-8"
    fieldVisibility = SpecificCompiler.FieldVisibility.PUBLIC
}
