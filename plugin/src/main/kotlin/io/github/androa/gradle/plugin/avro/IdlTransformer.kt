package io.github.androa.gradle.plugin.avro

import org.apache.avro.idl.IdlReader
import java.io.File
import java.io.PrintStream
import java.nio.file.Files

class IdlTransformer {
    /**
     * Transforms Avro IDL files to Avro Protocol files.
     * @return true if any IDL files were found and transformed, false otherwise.
     */
    fun transformIdl(
        inputDir: Set<File>,
        outputDir: File,
    ): Boolean {
        val hasIdlFiles = inputDir.any { it.isFile && it.extension == "avdl" }
        if (!hasIdlFiles) return false

        val parser = IdlReader()

        inputDir.filter { it.isFile && it.extension == "avdl" }.forEach { file ->
            // Create output file with same name but .avpr extension
            val outputFile = File(outputDir, file.nameWithoutExtension + ".avpr")
            outputFile.parentFile.mkdirs() // Ensure output directory exists

            val idlFile = parser.parse(file.inputStream())
            PrintStream(Files.newOutputStream(outputFile.toPath())).use { output ->
                val protocol = idlFile.protocol
                if (protocol != null) {
                    output.print(protocol.toString(true)) // Pretty print
                } else {
                    throw IllegalStateException("IDL file ${file.name} does not contain a protocol")
                }
            }
        }

        return true
    }
}
