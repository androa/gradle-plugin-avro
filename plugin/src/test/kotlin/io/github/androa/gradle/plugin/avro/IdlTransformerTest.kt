package io.github.androa.gradle.plugin.avro

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class IdlTransformerTest {
    @Test
    fun `test IDL files are transformed to protocols`() {
        val idlTransformer = IdlTransformer()
        val inputFiles = setOf(Path("src/test/resources/idl").toFile())
        val outputDir = Path("build/intermediates/avro/").toFile()

        idlTransformer.transformIdl(inputFiles, outputDir)

        // Add assertions to verify the transformation
        assertTrue(outputDir.exists())
    }
}
