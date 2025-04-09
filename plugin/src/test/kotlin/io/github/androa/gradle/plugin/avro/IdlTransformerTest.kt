package io.github.androa.gradle.plugin.avro

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class IdlTransformerTest {
    @Test
    fun `test IDL files are transformed to protocols`() {
        val idlTransformer = IdlTransformer()
        val inputFiles = File("src/test/resources/schemas/avdl/protocol.avdl")
        val outputDir = File("build/intermediates/avro/")

        idlTransformer.transformIdl(setOf(inputFiles), outputDir)

        // Add assertions to verify the transformation
        assertTrue(outputDir.exists())
        val outputFile = File(outputDir, "protocol.avpr")
        assertTrue(outputFile.exists(), "Protocol file should be generated")
    }

    @Test
    fun `test IDL files with imports are transformed correctly`(
        @TempDir tempDir: Path,
    ) {
        val idlTransformer = IdlTransformer()

        // Create test files with imports
        val typeFile = File(tempDir.toFile(), "types.avdl")
        //language=AvroIDL
        typeFile.writeText(
            """
            @namespace("test")
            protocol Types {
                record TestType {
                    string name;
                    long value;
                }
            }
            """.trimIndent(),
        )

        val mainFile = File(tempDir.toFile(), "main.avdl")
        //language=AvroIDL
        mainFile.writeText(
            """
            @namespace("test")
            protocol MyMain {
            	import idl "types.avdl";
            
            	record MainRecord {
            		TestType test;
            		string description;
            	}
            }
            """.trimIndent(),
        )

        val outputDir = File(tempDir.toFile(), "output")
        outputDir.mkdirs()

        // Test the transformer with files that include imports
        val result =
            idlTransformer.transformIdl(
                setOf(tempDir.toFile()),
                outputDir,
            )

        // Verify transformation succeeded
        assertTrue(result, "Transformation should indicate success")
        assertTrue(outputDir.resolve("types.avpr").exists(), "Types protocol file should be generated")
        assertTrue(outputDir.resolve("main.avpr").exists(), "Main protocol file should be generated")
    }
}
