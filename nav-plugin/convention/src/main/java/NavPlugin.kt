
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

        NavTransform.registerTransform(target)
    }
}