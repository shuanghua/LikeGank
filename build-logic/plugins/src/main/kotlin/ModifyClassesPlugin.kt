import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 例子参考：https://github.com/android/gradle-recipes
 */
class ModifyClassesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant: Variant ->
                // 使用 AsmClassVisitorFactory 修改字节码
                useAsmClassVisitorFactory(variant)
//                useToTransform(target, variant)
            }
        }
    }
}


// 仅限于 ASM 字节码插桩
private fun useAsmClassVisitorFactory(variant: Variant) {
    variant.instrumentation.transformClassesWith(
        AsmClassesVisitorFactory::class.java,
        InstrumentationScope.PROJECT
    ) {}
    variant.instrumentation
        .setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
}

// 待解决
// 遍历所有 class 文件，然后使用 asm 修改字节码 All class Transform  与 realm-java-android 冲突
private fun useToTransform(project: Project, variant: Variant) {

    val taskProvider = project.tasks.register("${variant.name}ModifyAllClassesTask")

    // Register modify classes task
    variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
        .use(taskProvider)
        .toTransform(
            type = ScopedArtifact.CLASSES,
            inputJars = { task -> (task as ModifyAllClassesTask).allJars },
            inputDirectories = { task -> (task as ModifyAllClassesTask).allDirectories },
            into = { task -> (task as ModifyAllClassesTask).output }
        )
}
