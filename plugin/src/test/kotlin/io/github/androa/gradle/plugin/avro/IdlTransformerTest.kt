package io.github.androa.gradle.plugin.avro

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class IdlTransformerTest {
    @Test
    fun `test IDL files are transformed to protocols`() {
        val idlTransformer = IdlTransformer()
        val inputFiles = setOf(File("src/test/resources/idl/idl-schema.avdl"))
        val outputDir = File("build/intermediates/avro/")

        idlTransformer.transformIdl(inputFiles, outputDir)

        // Add assertions to verify the transformation
        assertTrue(outputDir.exists())
    }
}
