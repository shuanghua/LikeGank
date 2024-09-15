package dev.shuanghua.library.nav

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
import javax.annotation.processing.Filer

/**
 * 使用 kotlinpoet 库生成 NavRegistry.kt 文件
 */
class KotlinpoetNavRegistryClass(
    private val outputDirectory: Filer,
    private val navDataList: MutableSet<NavData>
) {
    companion object {
        // 要生成 kotlin 文件的名字和对应的包名
        private const val class_name_nav_registry = "NavRegistry"
        private const val package_name = "dev.shuanghua.nav.runtime"
    }

    // 使用 kotlinPoet 生成 NavRegistry.kt 文件, 保存到 annotation 模块下以让 app 模块访问
    // 注意: KotlinPoet 生成的是源代码而不是字节码
    fun generateNavRegistryClass() {
//        println("发现${navDataList.size}个 NavDestination 注解. 现在开始生成 NavRegistry.kt 文件...")

        // import dev.shuanghua.annotations.NavData
        val navDataClass = ClassName("dev.shuanghua.annotations", "NavData") // NavData

        // import kotlin.collections.List 和 import kotlin.collections.ArrayList
        val listClass = ClassName("kotlin.collections", "List") // List<>
        val arrayListClass = ClassName("kotlin.collections", "ArrayList") // ArrayList<>

        // 组装 List<NavData> 和  ArrayList<NavData>
        val listWithNavData = listClass.parameterizedBy(navDataClass)
        val arrayListWithNavData = arrayListClass.parameterizedBy(navDataClass)

        val statements = StringBuilder()
        navDataList.forEach { it: NavData ->
            statements.append("navDataList.add(NavData(\"${it.route}\", \"${it.className}\", ${it.type.name}))\n")
        }

        // 定义全局属性:  private val navDataList: ArrayList<NavData> =  ArrayList()
        val propertyNavDataList = PropertySpec
            .builder("navDataList", arrayListWithNavData, KModifier.PRIVATE) // 声明属性
            .initializer(CodeBlock.builder().addStatement("ArrayList()").build()) // 实例化
            .build()


        // 定义函数: fun getNavDataList(): List<NavData> {}
        val getNavDataListFunSpec = FunSpec.builder("getNavDataList")
            .returns(listWithNavData)
            .addCode(
                CodeBlock.builder()
                    .addStatement("val list = ArrayList<NavData>()\nlist.addAll(navDataList)\nreturn list")
                    .build()
            ).build()

        // 生成流程开始
        //  object 类
        val objectTypeSpec = TypeSpec.objectBuilder(class_name_nav_registry)
            .addProperty(propertyNavDataList) // 添加全局属性 val navDataList
            .addInitializerBlock(
                CodeBlock.builder().addStatement(statements.toString()).build()
            ) // init 代码块
            .addFunction(getNavDataListFunSpec) // 添加函数 fun getNavDataList()
            .build()

        // 生成文件
        val fileSpec = FileSpec.builder(package_name, class_name_nav_registry)
            .addFileComment("该类是自动生成的, 请勿手动修改!")
            .addType(objectTypeSpec)
            .addImport(
                NavDestination.NavType::class.java.name.replace("$", "."),
                "Fragment", "Activity", "Dialog", "None"
            )
            .build()
        // 生成流程结束
        try {
            fileSpec.writeTo(System.out) // 打印到控制台
            fileSpec.writeTo(outputDirectory) // 生成文件
        } catch (e: Exception) {
            println("${e.message}")
        }
    }
}