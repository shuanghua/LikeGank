import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.objectweb.asm.ClassReader
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


/**
 *  Transform :  Google 提供的 Api
 *  Project : Gradle 提供的 Api
 *
 *  https://proandroiddev.com/make-gradle-do-more-work-for-you-than-just-build-your-android-app-9462baa08951
 */
class NavTransform(
    private val project: Project,
    private val allDirectories: ListProperty<Directory>,
    private val allJars: ListProperty<RegularFile>,
    private val output: RegularFileProperty
) {

    /**
     * Transform 方法是 Gradle 执行的入口方法，
     * 在这里我们需要做的就是遍历所有的 jar 包和 class 文件，
     * 然后对每个 class 文件进行处理。
     */
    internal fun transform() {
        val jarOutput = JarOutputStream(
            BufferedOutputStream(FileOutputStream(output.get().asFile))
        )
        handleClass(jarOutput)
        handleJar(jarOutput)
        jarOutput.close()
    }


    // 类文件处理
    private fun handleClass(jarOutput: JarOutputStream) {
        allDirectories.get().forEach { directory ->
            directory.asFile.walk().forEach { classFile: File ->
                if (classFile.isFile) {
                    if (classFile.name.endsWith("Fragment.class")) {
                        val inputStream = classFile.inputStream()
                        visitClass(inputStream)
                        inputStream.close()
                    }
                    val relativePath = directory.asFile.toURI().relativize(classFile.toURI()).path
                    jarOutput.writeClass(
                        relativePath.replace(File.separatorChar, '/'),
                        classFile.readBytes()
                    )
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
//                println("Jar 中的类文件名: ${jarEntry.name}")
                if (jarEntry.name.endsWith("Fragment.class")) {
                    val inputStream = jarFile.getInputStream(jarEntry)
                    visitClass(inputStream)
                    jarOutput.writeJar(jarEntry.name, inputStream)
                    inputStream.close()
                }
            }
            jarFile.close()
        }
    }

    // jar
    private fun JarOutputStream.writeJar(name: String, inputStream: InputStream) {
        putNextEntry(JarEntry(name))
        inputStream.copyTo(this)
        closeEntry()
    }


    // 类
    private fun JarOutputStream.writeClass(relativePath: String, byteArray: ByteArray) {
        putNextEntry(JarEntry(relativePath))
        write(byteArray)
        closeEntry()
    }


    private fun visitClass(inputStream: InputStream) {
        val classReader = ClassReader(inputStream)
        val classVisitor = ClassAnnotationVisitor(classReader, project)
        classReader.accept(classVisitor, 0)
    }
}



