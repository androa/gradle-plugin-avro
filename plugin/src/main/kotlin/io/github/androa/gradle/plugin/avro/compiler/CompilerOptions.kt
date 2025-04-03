package io.github.androa.gradle.plugin.avro.compiler

import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.generic.GenericData

data class CompilerOptions(
    var encoding: String? = null,
    var stringType: GenericData.StringType = GenericData.StringType.CharSequence,
    var fieldVisibility: SpecificCompiler.FieldVisibility? = null,
    var useLogicalDecimal: Boolean = false,
    var createSetters: Boolean = true,
    var createNullSafeAnnotations: Boolean = false,
    var addExtraOptionalGetters: Boolean = false,
    var optionalGettersType: OptionalGettersType? = null,
    var templateDir: String? = null,
)

enum class OptionalGettersType {
    ALL_FIELDS,
    ONLY_NULLABLE_FIELDS,
}
