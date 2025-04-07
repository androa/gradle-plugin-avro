package io.github.androa.gradle.plugin.avro

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AvroPluginFunctionalTest {
    @field:TempDir
    lateinit var projectDir: File

    @Test
    fun `plugin generates code successfully`() {
        // Create a minimal settings file so that Gradle recognizes the project.
        projectDir.resolve("settings.gradle").writeText("")

        // Create a minimal build.gradle.kts that applies the plugin
        projectDir.resolve("build.gradle.kts").writeText(
            """
            import org.apache.avro.compiler.specific.SpecificCompiler
            
            plugins {
                id("io.github.androa.gradle.plugin.avro")
                kotlin("jvm") version "2.1.20"
            }
            
            generateAvro {
                noSetters = true
                // Both assign and set() is possible
                addNullSafeAnnotations.set(true)
                encoding = "UTF-8"
                fieldVisibility = SpecificCompiler.FieldVisibility.PRIVATE 
                
                schemas.from(project.fileTree("src/main/avro"))
                outputDir.set(layout.buildDirectory.dir("generated-avro"))
            }
            """.trimIndent(),
        )

        // Create a dummy Avro schema file
        projectDir.resolve("src/main/avro/").apply {
            mkdirs()
            resolve("schema.avsc").writeText(
                // language=AvroSchema
                """
                {
                  "namespace": "com.example",
                  "type": "record",
                  "name": "Dummy",
                  "fields": [
                    { "name": "id", "type": "int" },
                    { "name": "age", "type": [ "null", "int" ] }
                  ]
                }
                """.trimIndent(),
            )
        }

        // Create a dummy Avro protocol file
        projectDir.resolve("src/main/avro/").apply {
            mkdirs()
            resolve("protocol.avpr").writeText(
                // language=AvroSchema
                """
                {
                  "protocol": "UserService",
                  "namespace": "com.example.avro",
                  "types": [
                    {
                      "type": "record",
                      "name": "User",
                      "fields": [
                        {
                          "name": "id",
                          "type": "string"
                        },
                        {
                          "name": "name",
                          "type": "string"
                        },
                        {
                          "name": "email",
                          "type": "string",
                          "default": ""
                        }
                      ]
                    }
                  ],
                  "messages": {
                    "getUser": {
                      "request": [
                        {
                          "name": "id",
                          "type": "string"
                        }
                      ],
                      "response": "User"
                    },
                    "createUser": {
                      "request": [
                        {
                          "name": "user",
                          "type": "User"
                        }
                      ],
                      "response": "string"
                    }
                  }
                }
                """.trimIndent(),
            )
        }

        // Create a dummy Avro IDL file
        projectDir.resolve("src/main/avro/").apply {
            mkdirs()
            resolve("protocol.avdl").writeText(
                // language=AvroIDL
                """
                @namespace("com.example.avro")
                protocol RecieptService {
                	record Receipt {
                		string id;
                		string name;
                		string email = "";
                	}

                	Receipt getReciept(string id);
                    string createReciept(Receipt reciept);
                }
                """.trimIndent(),
            )
        }

        // Run the generateAvro task
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath() // Makes the plugin available to the test build
            .withArguments("generateAvro", "--stacktrace")
            .build()

        // Verify that the output directory was created and contains generated files
        val outputDir = projectDir.resolve("build/generated-avro")
        assertTrue(
            outputDir.exists() && outputDir.listFiles()?.isNotEmpty() == true,
            "Expected generated Avro code in ${outputDir.absolutePath}",
        )

        with(outputDir.walk().filter { it.extension == "java" }) {
            assertTrue(any { it.name == "Dummy.java" }, "Expected Dummy.java in ${outputDir.absolutePath}")
            assertTrue(any { it.name == "User.java" }, "Expected User.java in ${outputDir.absolutePath}")
            assertTrue(any { it.name == "UserService.java" }, "Expected UserService.java in ${outputDir.absolutePath}")
            assertTrue(
                any { it.name == "RecieptService.java" },
                "Expected RecieptService.java in ${outputDir.absolutePath}",
            )
        }
    }
}
