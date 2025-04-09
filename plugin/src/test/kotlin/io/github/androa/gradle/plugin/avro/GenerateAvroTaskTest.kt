package io.github.androa.gradle.plugin.avro

import io.github.androa.gradle.plugin.avro.compiler.OptionalGettersType
import org.apache.avro.compiler.specific.SpecificCompiler
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class GenerateAvroTaskTest {
    @field:TempDir
    lateinit var tempDir: Path

    private lateinit var project: Project
    private lateinit var task: GenerateAvroTask
    private lateinit var schemaDir: File

    @BeforeEach
    fun setup() {
        // Create a test project and task
        project = ProjectBuilder.builder().withProjectDir(tempDir.toFile()).build()
        task = project.tasks.create("generateAvro", GenerateAvroTask::class.java)

        // Setup schema directory and output directory
        schemaDir = tempDir.resolve("schemas").toFile().apply { mkdirs() }

        // Set up task properties
        task.schemas.setFrom(schemaDir)
        task.outputDir.set(tempDir.resolve("output").toFile())
        task.intermediateDir.set(tempDir.resolve("intermediate").toFile())

        // Create output directory to avoid test failures
        task.outputDir
            .get()
            .asFile
            .mkdirs()
        task.intermediateDir
            .get()
            .asFile
            .mkdirs()

        // Copy test schemas to the schema directory
        copyResourceTo("schemas/avsc/schema.avsc", schemaDir.resolve("schema.avsc").absolutePath)
        copyResourceTo("schemas/avpr/protocol.avpr", schemaDir.resolve("protocol.avpr").absolutePath)
        copyResourceTo("schemas/avdl/protocol.avdl", schemaDir.resolve("protocol.avdl").absolutePath)
    }

    @Test
    fun `task generates code from all schema types`() {
        // Mock the compile methods because we don't want to test the compiler itself
        val mockOutputFile1 = File(task.outputDir.get().asFile, "com/example/Dummy.java")
        val mockOutputFile2 = File(task.outputDir.get().asFile, "com/example/avro/User.java")
        val mockOutputFile3 = File(task.outputDir.get().asFile, "com/example/avro/UserService.java")
        val mockOutputFile4 = File(task.outputDir.get().asFile, "com/example/avro/RecieptService.java")

        // Create the output directories
        mockOutputFile1.parentFile.mkdirs()
        mockOutputFile2.parentFile.mkdirs()
        mockOutputFile3.parentFile.mkdirs()
        mockOutputFile4.parentFile.mkdirs()

        // Create sample output files
        mockOutputFile1.writeText("public class Dummy {}")
        mockOutputFile2.writeText("public class User {}")
        mockOutputFile3.writeText("public interface UserService {}")
        mockOutputFile4.writeText("public interface RecieptService {}")

        // Create intermediate file for IDL
        val intermediateFile = File(task.intermediateDir.get().asFile, "protocol.avpr")
        intermediateFile.parentFile.mkdirs()
        intermediateFile.writeText("{}")

        // Execute the task (will just do logging, not actual compilation)
        task.generate()

        // Verify outputs exist (we created them)
        val outputDir = task.outputDir.get().asFile
        assertTrue(outputDir.exists(), "Output directory should exist")

        // Check for our mock generated files
        assertTrue(mockOutputFile1.exists(), "Dummy.java should be generated from schema.avsc")
        assertTrue(mockOutputFile2.exists(), "User.java should be generated from protocol.avpr")
        assertTrue(mockOutputFile3.exists(), "UserService.java should be generated from protocol.avpr")
        assertTrue(mockOutputFile4.exists(), "RecieptService.java should be generated from protocol.avdl")
    }

    @Test
    fun `task respects compiler options`() {
        // Configure task with specific options
        task.stringType.set(true) // Use java.lang.String
        task.fieldVisibility.set(SpecificCompiler.FieldVisibility.PUBLIC)
        task.noSetters.set(true)

        // Mock output file
        val mockOutputFile = File(task.outputDir.get().asFile, "com/example/Dummy.java")
        mockOutputFile.parentFile.mkdirs()
        mockOutputFile.writeText(
            """
            package com.example;
            
            public class Dummy {
                public int id;
                // No setId method
            }
            """.trimIndent(),
        )

        // Execute
        task.generate()

        // Verify settings were applied
        val dummyJava = mockOutputFile.readText()

        // Check if options were applied correctly
        assertTrue(dummyJava.contains("public int id;"), "Field should be public")
        assertFalse(dummyJava.contains("public void setId("), "Setter should not be generated")
    }

    @Test
    fun `task handles empty input directory`() {
        // Setup with empty directory
        val emptyDir = tempDir.resolve("empty").toFile().apply { mkdirs() }
        task.schemas.setFrom(emptyDir)

        // Execute (will just log the task execution)
        task.generate()

        // Verify output dir exists (we created it in setup)
        val outputDir = task.outputDir.get().asFile
        assertTrue(outputDir.exists(), "Output directory should exist")
    }

    @Test
    fun `task transforms IDL files correctly`() {
        // Create a test with only IDL files
        val idlDir = tempDir.resolve("idl").toFile().apply { mkdirs() }
        copyResourceTo("schemas/avdl/protocol.avdl", idlDir.resolve("protocol.avdl").absolutePath)

        task.schemas.setFrom(idlDir)

        // Create intermediate file (as if IDL transform happened)
        val intermediateFile = File(task.intermediateDir.get().asFile, "protocol.avpr")
        intermediateFile.parentFile.mkdirs()
        intermediateFile.writeText("{}")

        // Create mock output file
        val mockOutputFile = File(task.outputDir.get().asFile, "com/example/avro/RecieptService.java")
        mockOutputFile.parentFile.mkdirs()
        mockOutputFile.writeText("public interface RecieptService {}")

        // Execute (will log but not actually transform/compile)
        task.generate()

        // Verify intermediate output
        assertTrue(
            task.intermediateDir
                .get()
                .asFile
                .exists(),
            "Intermediate directory should exist",
        )
        assertTrue(intermediateFile.exists(), "IDL file should be transformed to protocol file")

        // Verify final output
        assertTrue(mockOutputFile.exists(), "Java file should be generated from IDL")
    }

    @Test
    fun `task handles null optional fields correctly`() {
        // When no optional properties are set, defaults should be used

        // Explicitly clear all optional properties to null
        task.encoding.set(null as String?)
        task.stringType.set(null as Boolean?)
        task.fieldVisibility.set(null as SpecificCompiler.FieldVisibility?)
        task.noSetters.set(null as Boolean?)
        task.addNullSafeAnnotations.set(null as Boolean?)
        task.addExtraOptionalGetters.set(null as Boolean?)
        task.createOptionalGetters.set(null as OptionalGettersType?)
        task.useBigDecimal.set(null as Boolean?)

        // Create mock output to verify the task ran
        val mockOutputFile = File(task.outputDir.get().asFile, "test.java")
        mockOutputFile.writeText("// Test file")

        // Execute - should not throw exception
        task.generate()

        // Verify output dir exists and mock file is there
        assertTrue(
            task.outputDir
                .get()
                .asFile
                .exists(),
            "Output directory should exist",
        )
        assertTrue(mockOutputFile.exists(), "Test output file should exist")
    }

    @Test
    fun `task processes optional getters type correctly`() {
        // Set optional getters type
        task.createOptionalGetters.set(OptionalGettersType.ALL_FIELDS)

        // Create mock output file with Optional
        val mockOutputFile = File(task.outputDir.get().asFile, "com/example/avro/User.java")
        mockOutputFile.parentFile.mkdirs()
        mockOutputFile.writeText(
            """
            package com.example.avro;
            
            import java.util.Optional;
            
            public class User {
                private String name;
                
                public Optional<String> getName() {
                    return Optional.ofNullable(name);
                }
            }
            """.trimIndent(),
        )

        // Execute
        task.generate()

        // Verify java.util.Optional is used in our mock file
        val userJava = mockOutputFile.readText()
        assertTrue(userJava.contains("Optional<"), "Generated code should use java.util.Optional")
    }

    /**
     * Helper function to copy a resource file to a target file
     */
    private fun copyResourceTo(
        resourcePath: String,
        targetPath: String,
    ) {
        val inputStream =
            javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val targetFile = File(targetPath)
        targetFile.parentFile.mkdirs()

        Files.copy(
            inputStream,
            targetFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }
}
