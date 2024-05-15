import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin

class NavPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withType(AppPlugin::class.java) {

            val androidComponents = target.extensions.getByType(
                AndroidComponentsExtension::class.java
            )

            androidComponents.onVariants { variant ->

                // 字节码修改
//                variant.instrumentation.transformClassesWith(
//                    NavClassVisitorFactory::class.java,
//                    InstrumentationScope.PROJECT
//                ) { params: InstrumentationParameters.None ->
////                params.
//                }

                // 注册一个 Task
                val taskProvider = target.tasks.register(
                    "${variant.name}NavTransform",
                    ReadClassesTask::class.java,
                )

                // 用于文件输出
                variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
                    .use<ReadClassesTask>(taskProvider)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        ReadClassesTask::allJars,
                        ReadClassesTask::allDirectories,
                        ReadClassesTask::output
                    )
            }
        }
    }
}