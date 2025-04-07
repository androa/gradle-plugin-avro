package io.github.androa.gradle.plugin.avro.compiler

import org.apache.avro.Protocol
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import java.io.File

class AvroCompiler {
    fun compileProtocol(
        compilerOpts: CompilerOptions = CompilerOptions(),
        inputDir: Set<File>,
        outputDir: File,
    ) {
        inputDir.avroProtocolFiles.forEach { src ->
            val protocol = Protocol.parse(src)
            val compiler = SpecificCompiler(protocol)
            executeCompiler(compiler, compilerOpts, src, outputDir)
        }
    }

    fun compileSchema(
        compilerOpts: CompilerOptions = CompilerOptions(),
        inputDir: Set<File>,
        outputDir: File,
    ) {
        val parser = Schema.Parser()
        inputDir.avroSchemaFiles.forEach { src ->
            val schema = parser.parse(src)
            val compiler = SpecificCompiler(schema)
            executeCompiler(compiler, compilerOpts, src, outputDir)
        }
    }

    private fun executeCompiler(
        compiler: SpecificCompiler,
        opts: CompilerOptions,
        src: File,
        output: File,
    ) {
        compiler.setStringType(opts.stringType)
        compiler.isCreateSetters = opts.createSetters
        compiler.isCreateNullSafeAnnotations = opts.createNullSafeAnnotations

        opts.optionalGettersType?.let { choice: OptionalGettersType ->
            compiler.isGettersReturnOptional = true
            when (choice) {
                OptionalGettersType.ALL_FIELDS -> compiler.isOptionalGettersForNullableFieldsOnly = false
                OptionalGettersType.ONLY_NULLABLE_FIELDS -> compiler.isOptionalGettersForNullableFieldsOnly = true
            }
        }

        compiler.isCreateOptionalGetters = opts.addExtraOptionalGetters
        opts.templateDir?.let { templateDir: String? -> compiler.setTemplateDir(templateDir) }
        compiler.setEnableDecimalLogicalType(opts.useLogicalDecimal)
        opts.encoding?.let { outputCharacterEncoding: String? ->
            compiler.setOutputCharacterEncoding(
                outputCharacterEncoding,
            )
        }
        opts.fieldVisibility?.let { fieldVisibility: SpecificCompiler.FieldVisibility? ->
            compiler.setFieldVisibility(
                fieldVisibility,
            )
        }
        compiler.compileToDestination(src, output)
    }

    private companion object {
        private val Set<File>.avroProtocolFiles: List<File>
            get() = filter { it.isFile && it.extension == "avpr" }.toList()

        private val Set<File>.avroSchemaFiles: List<File>
            get() = filter { it.isFile && it.extension == "avsc" }.toList()
    }
}
