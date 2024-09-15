import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

abstract class ModifyAllClassesTask : DefaultTask() {
    // This property will be set to all Jar files available in scope
    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    // Gradle will set this property with all class directories that available in scope
    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    // Task will put all classes from directories and jars after optional modification into single jar
    @get:OutputFile
    abstract val output: RegularFileProperty

    @Internal
    val jarPaths = mutableSetOf<String>()

    @TaskAction
    fun taskAction() {

        val jarOutput = JarOutputStream(
            BufferedOutputStream(FileOutputStream(output.get().asFile))
        )
        // copy classes from jar files without modification
        allJars.get().forEach { file ->
            println("handling " + file.asFile.absolutePath)
            val jarFile = JarFile(file.asFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                println("Adding from jar ${jarEntry.name}")
                jarOutput.writeEntity(jarEntry.name, jarFile.getInputStream(jarEntry))
            }
            jarFile.close()
        }
        // Iterating through class files from directories
        // Looking for SomeSource.class to add generated interface and instrument with additional output in
        // toString methods (in our case it's just System.out)
        allDirectories.get().forEach { directory ->
            println("handling " + directory.asFile.absolutePath)
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (file.name.endsWith(".class")) {
                        println("Found $file.name")

                    } else {
                        // 将其它文件复制到输出中，无需修改
                        val relativePath =
                            directory.asFile.toURI().relativize(file.toURI()).getPath()
                        println(
                            "Adding from directory ${
                                relativePath.replace(
                                    File.separatorChar,
                                    '/'
                                )
                            }"
                        )
                        jarOutput.writeEntity(
                            relativePath.replace(File.separatorChar, '/'),
                            file.inputStream()
                        )
                    }
                }
            }
        }
        jarOutput.close()
    }

    // writeEntity methods check if the file has name that already exists in output jar
    private fun JarOutputStream.writeEntity(name: String, inputStream: InputStream) {
        // check for duplication name first
        if (jarPaths.contains(name)) {
            printDuplicatedMessage(name)
        } else {
            putNextEntry(JarEntry(name))
            inputStream.copyTo(this)
            closeEntry()
            jarPaths.add(name)
        }
    }

    private fun JarOutputStream.writeEntity(relativePath: String, byteArray: ByteArray) {
        // check for duplication name first
        if (jarPaths.contains(relativePath)) {
            printDuplicatedMessage(relativePath)
        } else {
            putNextEntry(JarEntry(relativePath))
            write(byteArray)
            closeEntry()
            jarPaths.add(relativePath)
        }
    }

    private fun printDuplicatedMessage(name: String) =
        println("Cannot add ${name}, because output Jar already has file with the same name.")
}