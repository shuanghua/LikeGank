import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

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
        //  这里我们把具体的修改操作传给 NavTransform, 在 NavTransform 中使用 asm 对字节码进行修改
        //  当然也可以直接在这里修改,参考: https://github.com/android/gradle-recipes/blob/agp-7.4/Kotlin/modifyProjectClasses/app/build.gradle.kts
        NavTransform(
            project = project,
            allDirectories = allDirectories,
            allJars = allJars,
            output = output,
        ).transform()
    }
}