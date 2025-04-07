package io.github.androa.gradle.plugin.avro

import io.github.androa.gradle.plugin.avro.compiler.AvroCompiler
import io.github.androa.gradle.plugin.avro.compiler.CompilerOptions
import io.github.androa.gradle.plugin.avro.compiler.OptionalGettersType
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.generic.GenericData
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class GenerateAvroTask : DefaultTask() {
    private val compiler = AvroCompiler()

    @get:SkipWhenEmpty
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schemas: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val encoding: Property<String>

    @get:Input
    @get:Optional
    abstract val stringType: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val fieldVisibility: Property<SpecificCompiler.FieldVisibility>

    @get:Input
    @get:Optional
    abstract val noSetters: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val addNullSafeAnnotations: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val addExtraOptionalGetters: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val createOptionalGetters: Property<OptionalGettersType>

    @get:Input
    @get:Optional
    abstract val useBigDecimal: Property<Boolean>

    @TaskAction
    fun generate() {
        logger.info("Generating Avro data classes from ${schemas.asPath} to ${outputDir.get().asFile}")

        // Prepare the compiler options.
        val options = CompilerOptions()

        // Set the encoding of the output files.
        if (encoding.isPresent) {
            options.encoding = encoding.get()
        }

        // Use java.lang.String instead of Utf8
        if (stringType.isPresent) {
            options.stringType = GenericData.StringType.String
        }

        // Use either private or public field visibility. Defaults to private.
        if (fieldVisibility.isPresent) {
            options.fieldVisibility = fieldVisibility.get()
        }

        // Do not generate setters.
        if (noSetters.getOrElse(false)) {
            options.createSetters = false
        }

        // Add @Nullable and @NonNull annotations.
        if (addNullSafeAnnotations.getOrElse(false)) {
            options.createNullSafeAnnotations = true
        }

        // Generate extra getters with this format: 'getOptional<FieldName>'
        if (addExtraOptionalGetters.getOrElse(false)) {
            options.addExtraOptionalGetters = true
        }

        // Generate getters returning Optional<T> instead of T. Either all_fields or only nullable fields.
        if (createOptionalGetters.isPresent) {
            options.optionalGettersType = createOptionalGetters.get()
        }

        // Use java.math.BigDecimal for decimal type instaed of java.nio.ByteBuffer
        if (useBigDecimal.getOrElse(false)) {
            options.useLogicalDecimal = true
        }

        // Determine if the input directory contains Avro protocol files.
        val inputDir = schemas.files.toSet()
        val intermediateDir = project.file("build/intermediates/avro")

        // Transform any Avro IDL (.avdl) files to Avro protocol (.avpr) files.
        if (transformIdl(inputDir, intermediateDir)) {
            // Compile the Avro protocol and schema files.
            compiler.compileProtocol(options, intermediateDir.listFiles()!!.toSet(), outputDir.get().asFile)
        }

        // Compile any Avro protocol.
        compiler.compileProtocol(options, inputDir, outputDir.get().asFile)

        // Compile any Avro schema files.
        compiler.compileSchema(options, inputDir, outputDir.get().asFile)
    }

    private fun transformIdl(
        inputDir: Set<File>,
        outputDir: File,
    ) = IdlTransformer().transformIdl(inputDir, outputDir)
}
