import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "2.1.20"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.2")

    implementation("org.apache.avro:avro-compiler:1.12.0")
    implementation("org.apache.avro:avro-tools:1.12.0")
    implementation("org.apache.avro:avro:1.12.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
}

gradlePlugin {
    website.set("https://github.com/androa/gradle-plugin-avro")
    vcsUrl.set("https://github.com/androa/gradle-plugin-avro")

    plugins {
        create("avroGradlePlugin") {
            id = "io.github.androa.gradle.plugin.avro"
            group = "io.github.androa.gradle.plugin.avro"
            implementationClass = "io.github.androa.gradle.plugin.avro.AvroPlugin"
            version = "0.0.10"

            displayName = "Gradle Avro Plugin"
            description = "Gradle plugin for generating Java classes from Avro schemas."
            tags.set(listOf("avro", "data classes", "code generation"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
