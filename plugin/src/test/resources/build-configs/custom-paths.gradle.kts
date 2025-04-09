import org.apache.avro.compiler.specific.SpecificCompiler

plugins {
    id("io.github.androa.gradle.plugin.avro")
    kotlin("jvm") version "2.1.20"
}

generateAvro {
    noSetters = true
    addNullSafeAnnotations.set(false)
    encoding = "UTF-8"
    fieldVisibility = SpecificCompiler.FieldVisibility.PUBLIC
    
    schemas.from(project.fileTree("custom-avro-path"))
    outputDir.set(layout.buildDirectory.dir("custom-output-dir"))
}