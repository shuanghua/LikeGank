import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


/**
 *  Transform : 是 Google Android 提供的 Api
 *  Project : 是 Gradle 提供的 Api
 *
 *  https://proandroiddev.com/make-gradle-do-more-work-for-you-than-just-build-your-android-app-9462baa08951
 */
class NavTransform(
    private val inputs: ListProperty<Directory>,
    private val allJars: ListProperty<RegularFile>,
    private val output: RegularFileProperty
) {
    companion object {
        fun registerTransform(project: Project) {

            val androidComponents = project.extensions.getByType(
                AndroidComponentsExtension::class.java
            )

            androidComponents.onVariants { variant ->

                // 注册一个 Task
                val taskProvider = project.tasks.register(
                    "${variant.name}NavTransform",
                    ModifyClassesTask::class.java,
                )

                variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
                    .use<ModifyClassesTask>(taskProvider)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        ModifyClassesTask::allJars,
                        ModifyClassesTask::allDirectories,
                        ModifyClassesTask::output
                    )
            }
        }
    }


    /**
     * 修改字节码
     */
    internal fun transform() {
        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )


        allJars.get().forEach { file ->
            println("-->>正在处理的 Jar 文件 : ${file.asFile.absolutePath}")

            val jarFile = JarFile(file.asFile)

            jarFile.entries().iterator().forEach { jarEntry ->
                println("Jar 中发现类文件: ${jarEntry.name}")
                jarOutput.putNextEntry(JarEntry(jarEntry.name))
                jarFile.getInputStream(jarEntry).use {
                    it.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
            jarFile.close()

            inputs.get().forEach { directory ->
                println("直接类文件目录 : ${directory.asFile.absolutePath}")

                directory.asFile.walk().forEach { file ->
                    if (file.isFile) {
                        if (file.name.endsWith("Fragment.class")) {
                            println("找到 Fragment 的类文件如下: ${file.name}")

                            // TODO 使用 asm 对字节码进行修改

                        }
                    }
                }

            }

        }
        jarOutput.close()
    }
}


/**
 * 修改字节码的 Task
 */
abstract class ModifyClassesTask : DefaultTask() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty


    /**
     * 在这里可以直接使用 asm 对字节码修改
     * 但我们将修改操作放在 NavTransform ,这里只是中转
     */
    @TaskAction
    fun taskAction() {
        println("NavPlugin 查找类文件啦!!!!!")

        //  这里我们把具体的修改操作传给 NavTransform, 在 NavTransform 中使用 asm 对字节码进行修改
        //  当然也可以直接在这里修改,参考: https://github.com/android/gradle-recipes/blob/agp-7.4/Kotlin/modifyProjectClasses/app/build.gradle.kts
        NavTransform(
            inputs = allDirectories,
            allJars = allJars,
            output = output,
        ).transform()
    }
}

