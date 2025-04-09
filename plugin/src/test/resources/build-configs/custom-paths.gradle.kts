plugins {
    id("io.github.androa.gradle.plugin.avro")
    kotlin("jvm") version "2.1.20"
}

generateAvro {
    schemas.from(project.fileTree("custom-avro-path"))
    outputDir.set(layout.buildDirectory.dir("custom-output-dir"))
}
