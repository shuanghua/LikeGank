import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin

class NavPlugin : Plugin<Project> {
    // 插件调用入口
    override fun apply(target: Project) {
        println("NavPlugin 开始工作啦!!!!!")

        val applicationPlugin = target.plugins.findPlugin(ApplicationPlugin::class.java)
        assert(applicationPlugin == null) { throw GradleException("NavPlugin 必须在 app 模块中应用") }


        val androidComponents = target.extensions.getByType(
            AndroidComponentsExtension::class.java
        )

        androidComponents.onVariants { variant ->

//            variant.instrumentation.transformClassesWith(
//                NavClassVisitorFactory::class.java,
//                InstrumentationScope.PROJECT
//            ){ params ->
////                params.
//            }


            // 注册一个 Task
            val taskProvider = target.tasks.register(
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