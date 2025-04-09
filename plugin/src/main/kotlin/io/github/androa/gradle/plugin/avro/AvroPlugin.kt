package io.github.androa.gradle.plugin.avro

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider

class AvroPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create the extension to allow configuration
        val extension = project.extensions.create("generateAvro", AvroExtension::class.java)

        // Register the task
        val generateTask: TaskProvider<GenerateAvroTask> =
            project.tasks.register("generateAvro", GenerateAvroTask::class.java) {
                it.schemas.convention(extension.schemas)
                it.outputDir.convention(extension.outputDir)
                it.intermediateDir.convention(project.layout.buildDirectory.dir("intermediates/avro"))

                it.encoding.convention(extension.encoding)
                it.stringType.convention(extension.stringType)
                it.fieldVisibility.convention(extension.fieldVisibility)
                it.noSetters.convention(extension.noSetters)
                it.addNullSafeAnnotations.convention(extension.addNullSafeAnnotations)
                it.addExtraOptionalGetters.convention(extension.addExtraOptionalGetters)
                it.createOptionalGetters.convention(extension.createOptionalGetters)
                it.useBigDecimal.convention(extension.useBigDecimal)
            }

        // Make sure Kotlin compile tasks (or Java tasks if needed) depend on the generation task.
        project.tasks
            .matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }
            .configureEach { it.dependsOn(generateTask) }

        // Add the generated sources to the source set
        project.plugins.withId("java") {
            val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
            sourceSets.getByName("main").java.srcDir(generateTask.get().outputs.files)
        }
    }
}
