@file:Suppress("UnstableApiUsage")

package me.shedaniel.plugin.architect

import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.util.GradleSupport
import net.fabricmc.loom.util.TinyRemapperMappingsHelper
import net.fabricmc.mapping.tree.TinyTree
import net.fabricmc.tinyremapper.IMappingProvider
import net.fabricmc.tinyremapper.NonClassCopyMode
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.LinkedHashSet


open class RemapMCPTask : Jar() {
    private val fromM: String = "named"
    private val toM: String = "official"
    var fakeMod = false
    val input: RegularFileProperty = GradleSupport.getfileProperty(project)

    @TaskAction
    fun doTask() {
        val input: Path = this.input.asFile.get().toPath()
        val output: Path = this.archiveFile.get().asFile.toPath()

        output.toFile().delete()

        if (!Files.exists(input)) {
            throw FileNotFoundException(input.toString())
        }

        val remapperBuilder: TinyRemapper.Builder = TinyRemapper.newRemapper()

        val classpathFiles: Set<File> = LinkedHashSet(
                project.configurations.getByName("compileClasspath").files
        )
        val classpath = classpathFiles.asSequence().map { obj: File -> obj.toPath() }.filter { p: Path -> input != p && Files.exists(p) }.toList().toTypedArray()

        val mappings = getMappings()
        val mojmapToMcpClass = createMojmapToMcpClass(mappings)
        remapperBuilder.withMappings(remapToMcp(TinyRemapperMappingsHelper.create(mappings, fromM, fromM, false), mojmapToMcpClass))
        remapperBuilder.ignoreFieldDesc(true)
        remapperBuilder.skipLocalVariableMapping(true)

        project.logger.lifecycle(":remapping " + input.fileName)

        val architectFolder = project.rootProject.buildDir.resolve("tmp/architect")
        architectFolder.deleteRecursively()
        architectFolder.mkdirs()
        val fakeModId = "generated_" + UUID.randomUUID().toString().filterNot { it == '-' }.take(7)
        if (fakeMod) {
            val modsToml = architectFolder.resolve("META-INF/mods.toml")
            modsToml.parentFile.mkdirs()
            modsToml.writeText("""
modLoader = "javafml"
loaderVersion = "[33,)"
license = "Generated"
[[mods]]
modId = "$fakeModId"
        """.trimIndent())
            val mcmeta = architectFolder.resolve("pack.mcmeta")
            mcmeta.parentFile.mkdirs()
            mcmeta.writeText("""
{"pack":{"description":"Generated","pack_format":4}}
        """.trimIndent())
        }
        /*val manifestFile = architectFolder.resolve("META-INF/MANIFEST.MF")
        manifestFile.parentFile.mkdirs()
        manifestFile.writeText("""
Manifest-Version: 1.0
FMLModType: LIBRARY

        """.trimIndent())*/

        val remapper = remapperBuilder.build()

        try {
            OutputConsumerPath.Builder(output).build().use { outputConsumer ->
                outputConsumer.addNonClassFiles(input, NonClassCopyMode.SKIP_META_INF, null)
                outputConsumer.addNonClassFiles(architectFolder.toPath(), NonClassCopyMode.UNCHANGED, null)
                remapper.readClassPath(*classpath)
                remapper.readInputs(input)
                remapper.apply(outputConsumer)

                if (fakeMod) {
                    val className = "generated/$fakeModId"
                    val classWriter = ClassWriter(0)
                    classWriter.visit(52, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null)
                    val mixinAnnotation = classWriter.visitAnnotation("Lnet/minecraftforge/fml/common/Mod;", false)
                    mixinAnnotation.visit("value", fakeModId)
                    mixinAnnotation.visitEnd()
                    classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, arrayOf()).also {
                        it.visitVarInsn(Opcodes.ALOAD, 0)
                        it.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
                        it.visitInsn(Opcodes.RETURN)
                        it.visitMaxs(1, 1)
                        it.visitEnd()
                    }
                    classWriter.visitEnd()
                    outputConsumer.accept(className, classWriter.toByteArray())
                }
            }
        } catch (e: Exception) {
            remapper.finish()
            throw RuntimeException("Failed to remap $input to $output", e)
        }

        architectFolder.deleteRecursively()
        remapper.finish()

        if (!Files.exists(output)) {
            throw RuntimeException("Failed to remap $input to $output - file missing!")
        }
    }

    private fun remapToMcp(parent: IMappingProvider, mojmapToMcpClass: Map<String, String>): IMappingProvider = IMappingProvider {
        it.acceptClass("net/fabricmc/api/Environment", "net/minecraftforge/api/distmarker/OnlyIn")
        it.acceptClass("net/fabricmc/api/EnvType", "net/minecraftforge/api/distmarker/Dist")
        it.acceptField(IMappingProvider.Member("net/fabricmc/api/EnvType", "SERVER", "Lnet/fabricmc/api/EnvType;"), "DEDICATED_SERVER")

        parent.load(object : IMappingProvider.MappingAcceptor {
            override fun acceptClass(srcName: String?, dstName: String?) {
                it.acceptClass(srcName, mojmapToMcpClass[srcName] ?: srcName)
            }

            override fun acceptMethod(method: IMappingProvider.Member?, dstName: String?) {
            }

            override fun acceptMethodArg(method: IMappingProvider.Member?, lvIndex: Int, dstName: String?) {
            }

            override fun acceptMethodVar(method: IMappingProvider.Member?, lvIndex: Int, startOpIdx: Int, asmIndex: Int, dstName: String?) {
            }

            override fun acceptField(field: IMappingProvider.Member?, dstName: String?) {
            }
        })
    }

    private fun getMappings(): TinyTree {
        val loomExtension = project.extensions.getByType(LoomGradleExtension::class.java)
        return loomExtension.mappingsProvider.mappings
    }

    private fun getRootExtension(): ArchitectPluginExtension =
            project.rootProject.extensions.getByType(ArchitectPluginExtension::class.java)

    private fun createMojmapToMcpClass(mappings: TinyTree): Map<String, String> {
        val mcpMappings = readMCPMappings(getRootExtension().minecraft)
        val mutableMap = mutableMapOf<String, String>()
        mappings.classes.forEach { clazz ->
            val official = clazz.getName("official")
            val named = clazz.getName("named")
            val mcp = mcpMappings[official]
            if (mcp != null) {
                mutableMap[named] = mcp
            }
        }
        return mutableMap
    }

    private fun readMCPMappings(version: String): Map<String, String> {
        val file = project.rootProject.file(".gradle/mappings/mcp-$version.tsrg")
        if (file.exists().not()) {
            file.parentFile.mkdirs()
            file.writeText(URL("https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/$version/joined.tsrg").readText())
        }
        return mutableMapOf<String, String>().also { readMappings(it, file.inputStream()) }
    }

    private fun readMappings(mutableMap: MutableMap<String, String>, inputStream: InputStream) {
        inputStream.bufferedReader().forEachLine {
            if (!it.startsWith("\t")) {
                val split = it.split(" ")
                val obf = split[0]
                val className = split[1]
                mutableMap[obf] = className
            }
        }
    }
}