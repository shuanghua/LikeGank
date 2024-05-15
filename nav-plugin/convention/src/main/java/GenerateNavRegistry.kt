import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.shuanghua.annotations.NavData
import dev.shuanghua.annotations.NavDestination
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

/**
 * 生成 NavRegistry.kt 文件
*/
class GenerateNavRegistry(
    private val project: Project,
    private val navDataList: MutableList<NavData>
) {
    companion object {
        private const val class_name_nav_data = "NavData"
        private const val class_name_nav_registry = "NavRegistry"
        private const val property_name_nav_list = "navList"
        private const val module_name_annotation = "annotation"
        private const val package_name_annotation = "dev.shuanghua.annotations"
    }

    // 使用 kotlinPoet 生成 NavRegistry.kt 文件, 保存到 annotation 模块下以让 app 模块访问
    // 注意: KotlinPoet 生成的是源代码而不是字节码
     internal fun generateNavRegistryClass() {
        println("------------------------------------------------------------")
        println("1.注解个数: ${navDataList.size} 个")

        println("2.开始生成 NavRegistry.kt 文件...")

        val navDataClass = ClassName(package_name_annotation, class_name_nav_data) // NavData

        val arrayListClass = ClassName("kotlin.collections", "ArrayList") // ArrayList<>
        val arrayListNavData = arrayListClass.parameterizedBy(navDataClass) // ArrayList<NavData>

        val listClass = ClassName("kotlin.collections", "List") // ArrayList<>
        val listNavData = listClass.parameterizedBy(navDataClass) // ArrayList<NavData>

        val statements = StringBuilder()
        navDataList.forEach {it: NavData ->
            statements.append("navList.add(NavData(\"${it.route}\", \"${it.className}\", ${it.type.name}))\n")
        }

        // 全局属性
        val property1 = PropertySpec
            .builder(property_name_nav_list,arrayListNavData, KModifier.PRIVATE) // 声明属性
            .initializer(CodeBlock.builder().addStatement("ArrayList()").build()) // 实例化
            .build()

        // 函数
        val funSpec = FunSpec.builder("get")
            .returns(listNavData)
            .addCode(
                CodeBlock.builder()
                    .addStatement("val list = ArrayList<NavData>()\nlist.addAll(navList)\nreturn list")
                    .build())
            .build()

        // 类名, 这里使用 object 关键字
        val typeSpec = TypeSpec.objectBuilder(class_name_nav_registry)
            .addProperty(property1)
            .addInitializerBlock(CodeBlock.builder().addStatement(statements.toString()).build())
            .addFunction(funSpec)
            .build()

        // 生成文件
        val fileSpec = FileSpec.builder(package_name_annotation, class_name_nav_registry)
            .addFileComment("该类是自动生成的, 请勿手动修改!")
            .addType(typeSpec)
            .addImport(NavDestination.NavType::class.java.name.replace("$", "."), "Fragment", "Activity", "Dialog", "None")
            .build()

        // 写入文件到指定位置 (这里生成到 NavData.kt 文件所在的目录)
        val annotationProject = project.rootProject.findProject(module_name_annotation)
        checkNotNull(annotationProject){ "annotation 模块不存在!" }
        val sourceSets = annotationProject.extensions.findByName("sourceSets") as SourceSetContainer
        val outputDir: File = sourceSets.first().java.srcDirs.first().absoluteFile
        fileSpec.writeTo(outputDir)

        println("3.代码生成: $outputDir")
        println("------------------------------------------------------------")
//        fileSpec.writeTo(System.out) // 测试-打印到控制台
    }
}