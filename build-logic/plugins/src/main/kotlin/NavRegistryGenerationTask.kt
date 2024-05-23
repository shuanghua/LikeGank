import dev.shuanghua.annotations.NavData
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

abstract class NavRegistryGenerationTask : DefaultTask() {

    private val navDataList: MutableList<NavData> = mutableListOf()

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty


    @TaskAction
    fun taskAction() {
        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(output.get().asFile)
            )
        )

        handleJar(jarOutput)
        handleClass(jarOutput)
        jarOutput.close()

        // 生成 NavRegistry 类
        val ge = GenerateNavRegistry(project, navDataList)
        ge.generateNavRegistryClass()
    }

    // 类文件处理
    private fun handleClass(jarOutput: JarOutputStream) {
        allDirectories.get().forEach { directory ->
            directory.asFile.walk().forEach { file: File ->
                println("正在处理的 class 文件 : ${file.absolutePath}")
                if (file.isFile) {
                    val inputStream = file.inputStream()
                    if (file.name.endsWith("Fragment.class")) { visitClass(inputStream) }
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                    jarOutput.copyTo(relativePath.replace(File.separatorChar, '/'), file.inputStream())
                    inputStream.close()
                }
            }
        }
    }


    // jar 包处理
    private fun handleJar(jarOutput: JarOutputStream) {
        allJars.get().forEach { file ->
//            println("-->>正在处理的 Jar 文件 : ${file.asFile.absolutePath}")
            val jarFile = JarFile(file.asFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                jarOutput.copyTo(jarEntry.name, jarFile.getInputStream(jarEntry))
            }
            jarFile.close()
        }
    }

    // jar
    private fun JarOutputStream.copyTo(name: String, inputStream: InputStream) {
        putNextEntry(JarEntry(name))
        inputStream.copyTo(this)
        closeEntry()
    }


    // 如果对 class 文件进行了修改，则调用该方法将修改后的字节码写入 jar 包中
    private fun JarOutputStream.writeClass(relativePath: String, byteArray: ByteArray) {
        putNextEntry(JarEntry(relativePath))
        write(byteArray)
        closeEntry()
    }

    // 使用 ASM 库提取类文件中的注解信息
    private fun visitClass(inputStream: InputStream) {
        val classReader = ClassReader(inputStream)
        classReader.accept(AsmAnnotationClassVisitor(navDataList), ClassReader.EXPAND_FRAMES)
    }
}