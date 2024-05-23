import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

class NavPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withType(AppPlugin::class.java) {
            val androidComponents  = target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant: Variant ->
                useAsmClassVisitorFactory(variant)
            }
        }
    }
}


// 仅限于 ASM 字节码插桩
private fun useAsmClassVisitorFactory(variant: Variant){
    variant.instrumentation.transformClassesWith(
        AsmClassesVisitorFactory::class.java,
        InstrumentationScope.PROJECT) {
    }
    variant.instrumentation
        .setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
}

// All class Transform  与 realm-java-android 冲突，暂时不使用
private fun useToTransform(project: Project, variant: Variant){
    val taskName = "${variant.name}VisitorAnnotationClass"
    val taskProvider = project.tasks.register(
        taskName,
        NavRegistryGenerationTask::class.java
    ){
        // 在这里可给 CheckFragmentClassTask 自定义的属性赋值
    }
    variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
        .use(taskProvider)
        .toTransform(
            ScopedArtifact.CLASSES,
            NavRegistryGenerationTask::allJars,
            NavRegistryGenerationTask::allDirectories,
            NavRegistryGenerationTask::output
        )
}