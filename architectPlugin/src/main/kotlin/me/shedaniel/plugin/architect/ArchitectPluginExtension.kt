package me.shedaniel.plugin.architect

import org.gradle.api.Project

open class ArchitectPluginExtension(val project: Project) {
    var minecraft = ""

    fun common() {
        project.configurations.create("mcp")
        project.configurations.create("mcpGenerateMod")

        project.tasks.getByName("remapMcp") {
            it as RemapMCPTask

            it.input.set(project.file("${project.buildDir}/libs/${project.properties["archivesBaseName"]}-${project.version}-dev.jar"))
            it.archiveClassifier.set("mcp")
            it.dependsOn(project.tasks.getByName("jar"))
            project.tasks.getByName("build").dependsOn(it)
            it.outputs.upToDateWhen { false }
        }

        project.tasks.getByName("remapMcpFakeMod") {
            it as RemapMCPTask

            it.input.set(project.file("${project.buildDir}/libs/${project.properties["archivesBaseName"]}-${project.version}-dev.jar"))
            it.archiveClassifier.set("mcpGenerateMod")
            it.dependsOn(project.tasks.getByName("jar"))
            project.tasks.getByName("build").dependsOn(it)
            it.outputs.upToDateWhen { false }
        }

        project.artifacts {
            it.add("mcp", mapOf(
                    "file" to project.file("${project.buildDir}/libs/${project.properties["archivesBaseName"]}-${project.version}-mcp.jar"),
                    "type" to "jar",
                    "builtBy" to project.tasks.getByName("remapMcp")
            ))
            it.add("mcpGenerateMod", mapOf(
                    "file" to project.file("${project.buildDir}/libs/${project.properties["archivesBaseName"]}-${project.version}-mcpGenerateMod.jar"),
                    "type" to "jar",
                    "builtBy" to project.tasks.getByName("remapMcpFakeMod")
            ))
        }
    }
}