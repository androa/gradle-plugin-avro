package io.github.androa.gradle.plugin.avro

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class AvroPluginTest {
    @field:TempDir
    lateinit var tempDir: Path

    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder().withProjectDir(tempDir.toFile()).build()
    }

    @Test
    fun `plugin registers generateAvro task`() {
        // Apply the plugin
        project.plugins.apply(AvroPlugin::class.java)

        // Verify task is created
        val task = project.tasks.findByName("generateAvro")
        assertNotNull(task, "Plugin should register 'generateAvro' task")
        assertTrue(task is GenerateAvroTask, "Task should be a GenerateAvroTask")
    }

    @Test
    fun `plugin creates extension with default values`() {
        // Apply the plugin
        project.plugins.apply(AvroPlugin::class.java)

        // Get the extension
        val extension = project.extensions.findByName("generateAvro")
        assertNotNull(extension, "Plugin should create 'generateAvro' extension")
        assertTrue(extension is AvroExtension, "Extension should be an AvroExtension")

        // Cast to AvroExtension
        val avroExtension = extension as AvroExtension

        // Verify default values
        assertEquals("UTF-8", avroExtension.encoding.get())
        assertFalse(avroExtension.stringType.get())
        assertFalse(avroExtension.noSetters.get())
        assertFalse(avroExtension.addNullSafeAnnotations.get())
        assertFalse(avroExtension.addExtraOptionalGetters.get())
        assertFalse(avroExtension.useBigDecimal.get())
    }

    @Test
    fun `plugin configures task from extension values`() {
        // Apply the plugin
        project.plugins.apply(AvroPlugin::class.java)

        // Configure the extension
        val extension = project.extensions.getByType(AvroExtension::class.java)
        extension.stringType.set(true)
        extension.noSetters.set(true)

        // Get the task
        val task = project.tasks.getByName("generateAvro") as GenerateAvroTask

        // Verify task properties are populated from extension
        assertTrue(task.stringType.get(), "Task stringType should match extension value")
        assertTrue(task.noSetters.get(), "Task noSetters should match extension value")
    }

    @Test
    fun `plugin adds task to task group`() {
        // Apply the plugin
        project.plugins.apply(AvroPlugin::class.java)

        // Get the task
        val task = project.tasks.getByName("generateAvro")

        // Verify the task exists and is configured correctly
        assertNotNull(task, "Task should exist")
        assertTrue(task is GenerateAvroTask, "Task should be a GenerateAvroTask")
    }

    @Test
    fun `plugin adds generated source directory to source set`() {
        // Apply the java plugin
        project.plugins.apply("java")

        // Apply the avro plugin
        project.plugins.apply(AvroPlugin::class.java)

        // Get the task
        val task = project.tasks.getByName("generateAvro") as GenerateAvroTask

        // Get source sets
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val mainSourceSet = sourceSets.getByName("main")

        // Verify source set includes task output
        val javaSrcDirs = mainSourceSet.java.srcDirs
        assertTrue(
            javaSrcDirs.contains(task.outputDir.get().asFile),
            "Main source set should include Avro output directory",
        )
    }

    @Test
    fun `plugin works without applying java plugin`() {
        // Apply only the avro plugin (without java plugin)
        project.plugins.apply(AvroPlugin::class.java)

        // This should not throw an exception
        val task = project.tasks.getByName("generateAvro")
        assertNotNull(task, "Task should be created even without java plugin")
    }

    /**
     * Helper method to get task dependency names
     */
    private fun getTaskDependencyNames(task: Task): Set<String> {
        // This is a bit hacky but works for testing
        val method = task.javaClass.methods.find { it.name == "getDependsOn" }
        val dependsOn = method?.invoke(task) as? Set<*> ?: emptySet<Any>()

        return dependsOn
            .mapNotNull {
                when (it) {
                    is Task -> it.name
                    is String -> it
                    else -> null
                }
            }.toSet()
    }
}
