package io.github.androa.gradle.plugin.avro

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AvroPluginFunctionalTest {
    @field:TempDir
    lateinit var projectDir: File

    @ParameterizedTest
    @ValueSource(
        strings = [
            "zero-config.gradle.kts",
            "default.gradle.kts",
            "custom-paths.gradle.kts",
        ],
    )
    fun `plugin generates code successfully with different build configs`(buildConfigFile: String) {
        // Create a minimal settings file
        projectDir.resolve("settings.gradle").writeText("")

        // Copy the build config from resources
        copyResourceTo("build-configs/$buildConfigFile", "build.gradle.kts")

        // For the custom paths test, we need to adjust the directory structure
        if (buildConfigFile == "custom-paths.gradle.kts") {
            setupSchemaFiles("custom-avro-path")
            runAndVerify("build/custom-output-dir")
        } else {
            setupSchemaFiles()
            runAndVerify()
        }
    }

    /**
     * Helper function to copy a resource file to the test project directory
     */
    private fun copyResourceTo(
        resourcePath: String,
        targetPath: String,
    ) {
        val inputStream =
            javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val targetFile = projectDir.resolve(targetPath)
        targetFile.parentFile.mkdirs()

        Files.copy(
            inputStream,
            targetFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }

    /**
     * Helper function to set up schema files in the project directory
     */
    private fun setupSchemaFiles(targetDir: String = "src/main/avro") {
        // Copy AVSC schema
        copyResourceTo("schemas/avsc/schema.avsc", "$targetDir/schema.avsc")

        // Copy AVPR protocol
        copyResourceTo("schemas/avpr/protocol.avpr", "$targetDir/protocol.avpr")

        // Copy AVDL file
        copyResourceTo("schemas/avdl/protocol.avdl", "$targetDir/protocol.avdl")
    }

    /**
     * Helper function to run the generateAvro task and verify outputs
     */
    private fun runAndVerify(outputDir: String = "build/generated/sources/avro") {
        // Run the generateAvro task
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath() // Makes the plugin available to the test build
            .withArguments("generateAvro", "--stacktrace")
            .build()

        // Verify that the output directory was created and contains generated files
        val generatedDir = projectDir.resolve(outputDir)
        assertTrue(
            generatedDir.exists() && generatedDir.listFiles()?.isNotEmpty() == true,
            "Expected generated Avro code in ${generatedDir.absolutePath}",
        )

        with(generatedDir.walk().filter { it.extension == "java" }) {
            assertTrue(any { it.name == "Dummy.java" }, "Expected Dummy.java in ${generatedDir.absolutePath}")
            assertTrue(any { it.name == "User.java" }, "Expected User.java in ${generatedDir.absolutePath}")
            assertTrue(
                any { it.name == "UserService.java" },
                "Expected UserService.java in ${generatedDir.absolutePath}",
            )
            assertTrue(
                any { it.name == "RecieptService.java" },
                "Expected RecieptService.java in ${generatedDir.absolutePath}",
            )
        }
    }
}
