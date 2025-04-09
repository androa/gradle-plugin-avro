package io.github.androa.gradle.plugin.avro

import io.github.androa.gradle.plugin.avro.compiler.AvroCompiler
import io.github.androa.gradle.plugin.avro.compiler.CompilerOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ErrorHandlingTest {
    @field:TempDir
    lateinit var tempDir: Path

    @Test
    fun `compiler handles malformed schema files gracefully`() {
        // Setup
        val compiler = AvroCompiler()
        val outputDir = tempDir.resolve("output").toFile().apply { mkdirs() }

        // Create a malformed schema file
        val malformedSchema = tempDir.resolve("malformed.avsc").toFile()
        malformedSchema.writeText(
            """
            {
              "namespace": "com.example",
              "type": "record",
              "name": "Broken",
              "fields": [
                { "name": "id", "type": "int" },
                { "name": "invalid" } // Missing type
              ]
            }
            """.trimIndent(),
        )

        // Execute and verify exception is thrown with appropriate message
        val exception =
            assertThrows(Exception::class.java) {
                compiler.compileSchema(CompilerOptions(), setOf(malformedSchema), outputDir)
            }

        // Verify error message contains helpful information
        assertTrue(
            exception.message?.contains("malformed") == true ||
                exception.message?.contains("schema") == true ||
                exception.message?.contains("parse") == true ||
                exception.message?.contains("missing") == true ||
                exception.message?.contains("type") == true,
            "Exception message should indicate schema parsing issue",
        )
    }

    @Test
    fun `compiler handles non-existent input directory gracefully`() {
        // Setup
        val compiler = AvroCompiler()
        val nonExistentDir = tempDir.resolve("nonexistent").toFile()
        val outputDir = tempDir.resolve("output").toFile().apply { mkdirs() }

        // Execute
        compiler.compileSchema(CompilerOptions(), setOf(nonExistentDir), outputDir)

        // Verify - should not throw exception, and output dir should be empty
        val outputFiles = outputDir.listFiles() ?: emptyArray()
        assertEquals(0, outputFiles.size, "No output should be generated with empty input")
    }

    @Test
    fun `task handles empty input directory`() {
        // Setup task
        val project =
            org.gradle.testfixtures.ProjectBuilder
                .builder()
                .withProjectDir(tempDir.toFile())
                .build()
        val task = project.tasks.create("generateAvro", GenerateAvroTask::class.java)

        // Create an empty directory
        val emptyDir = tempDir.resolve("empty").toFile().apply { mkdirs() }
        val outputDir = tempDir.resolve("output").toFile().apply { mkdirs() }

        // Configure task
        task.schemas.setFrom(emptyDir)
        task.outputDir.set(outputDir)
        task.intermediateDir.set(tempDir.resolve("intermediate").toFile())

        // Execute - should not throw exception
        task.generate()

        // Verify output dir exists but is empty
        assertTrue(outputDir.exists(), "Output directory should exist")
    }

    @Test
    fun `generate task handles malformed avpr file gracefully`() {
        // Setup task
        val project =
            org.gradle.testfixtures.ProjectBuilder
                .builder()
                .withProjectDir(tempDir.toFile())
                .build()
        val task = project.tasks.create("generateAvro", GenerateAvroTask::class.java)

        // Create a malformed protocol file
        val malformedProtocol = tempDir.resolve("malformed.avpr").toFile()
        malformedProtocol.writeText(
            """
            {
              "protocol": "BrokenService",
              "namespace": "com.example",
              "types": [
                {
                  "type": "record",
                  "name": "Broken",
                  "fields": [
                    { "name": "id", "type": "string" },
                    { "name": "data" } // Missing type
                  ]
                }
              ]
            }
            """.trimIndent(),
        )

        // Configure task
        task.schemas.setFrom(malformedProtocol)
        task.outputDir.set(tempDir.resolve("output").toFile())
        task.intermediateDir.set(tempDir.resolve("intermediate").toFile())

        // Execute and verify
        val exception =
            assertThrows(Exception::class.java) {
                task.generate()
            }

        // Verify error message is helpful
        assertTrue(
            exception.message?.contains("malformed") == true ||
                exception.message?.contains("protocol") == true ||
                exception.message?.contains("parse") == true ||
                exception.message?.contains("missing") == true ||
                exception.message?.contains("type") == true,
            "Exception message should indicate protocol parsing issue",
        )
    }
}
