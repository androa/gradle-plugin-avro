package io.github.androa.gradle.plugin.avro

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class IdlTransformerTest {
    @Test
    fun foobar() {
        val idlTransformer = IdlTransformer()
        val inputDir = Path("src/test/resources/idl").toFile()
        val outputDir = Path("build/intermediates/avro/").toFile()
        idlTransformer.transformIdl(inputDir, outputDir)

        // Add assertions to verify the transformation
        assertTrue(inputDir.exists())
    }
}
