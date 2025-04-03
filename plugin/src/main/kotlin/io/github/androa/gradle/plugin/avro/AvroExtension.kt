package io.github.androa.gradle.plugin.avro

import io.github.androa.gradle.plugin.avro.compiler.OptionalGettersType
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

abstract class AvroExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        /**
         * Directory containing Avro schema (.avsc) files.
         * Default: src/main/avro
         */
        abstract val schemasDir: DirectoryProperty

        /**
         * Directory where generated sources will be placed.
         * Default: build/generated-src/avro
         */
        abstract val outputDir: DirectoryProperty

        /**
         * Set the encoding of the output files.
         * Possible values: UTF-8, UTF-16, etc.
         * Default: UTF-8
         */
        abstract val encoding: Property<String>

        /**
         * When true, use java.lang.String instead of Utf8
         * Default: false
         */
        abstract val stringType: Property<Boolean>

        /**
         * Specifies the field visibility for generated classes.
         * Possible values is either "private" or "public"
         * Default: private
         */
        abstract val fieldVisibility: Property<FieldVisibility>

        /**
         * When true, do not generate setters.
         * Default: false
         */
        abstract val noSetters: Property<Boolean>

        /**
         * When true, add @Nullable and @NonNull annotations.
         * Default: false
         */
        abstract val addNullSafeAnnotations: Property<Boolean>

        /**
         * When true, generate extra getters with this format: 'getOptional<FieldName>'
         * Default: false
         */
        abstract val addExtraOptionalGetters: Property<Boolean>

        /**
         * Generate getters returning Optional<T> instead of T. Either all_fields or only nullable fields.
         * Default: none
         */
        abstract val createOptionalGetters: Property<OptionalGettersType>

        /**
         * When true, use java.math.BigDecimal for decimal type instaed of java.nio.ByteBuffer
         * Default: false
         */
        abstract val useBigDecimal: Property<Boolean>

        init {
            schemasDir.convention(objects.directoryProperty().fileValue(File("src/main/avro")))
            outputDir.convention(objects.directoryProperty().fileValue(File("build/generated/sources/avro")))

            encoding.convention("UTF-8")
            stringType.convention(false)
            fieldVisibility.convention(FieldVisibility.PRIVATE)
            noSetters.convention(false)
            addNullSafeAnnotations.convention(false)
            addExtraOptionalGetters.convention(false)
            useBigDecimal.convention(false)
        }
    }
