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
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import java.io.File
import java.lang.reflect.Modifier

/**
 * 专门处理类上面的注解信息
 */
class ClassAnnotationVisitor(
    private val classReader: ClassReader,
    private val project: Project,
) : ClassVisitor(Opcodes.ASM9) {
    private val navDataList = mutableListOf<NavData>()

    companion object {
        const val nav_annotation_destination = "Ldev/shuanghua/annotations/NavDestination;"
        const val nav_annotation_type = "Ldev/shuanghua/annotations/NavDestination\$NavType;"

        private const val key_route = "route"
        private const val key_type = "type"
        private const val class_name_nav_data = "NavData"
        private const val class_name_nav_registry = "NavRegistry"
        private const val property_name_nav_list = "navList"
        private const val module_name_annotation = "annotation"
        private const val package_name_annotation = "dev.shuanghua.annotations"

    }

    private var className: String? = null
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
    }


    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        println("------------------------------------------------------------")
        println("1.发现注解: @NavDestination -> $className")


        if (descriptor != nav_annotation_destination) {
            return object : AnnotationVisitor(Opcodes.ASM9) {}
        }


        return object : AnnotationNode(Opcodes.ASM9, "") {
            var route = ""
            var type = NavDestination.NavType.None

            // 如果注解里面的键值对
            override fun visit(name: String?, value: Any?) {
                super.visit(name, value)
//                println("发现键值对: key=$name value=$value")
                if (name == key_route) {
                    // 解析路由信息
                    route = value.toString()
                }
            }

            // 如果注解里面的枚举
            override fun visitEnum(name: String?, descriptor: String?, value: String?) {
                super.visitEnum(name, descriptor, value)
//                println("发现枚举: key=$name 枚举=$descriptor 枚举值=$value")
                if (name == key_type) {
                    // 解析枚举类型信息
                    assert(value != null) { throw GradleException("NavType 枚举值不能为空!") }
                    type = NavDestination.NavType.valueOf(value!!)
                }
            }

            // 解析完成所有注解, 在这里可以将解析的注解信息统一封装到一个类中
            override fun visitEnd() {
                super.visitEnd()
                val navData = NavData(
                    route,
                    className = classReader.className.replace("/", "."),
                    type
                )
                navDataList.add(navData)
                println("2.注解解析: $navDataList")
                generateNavRegistryClass()
            }
        }
    }


    // 使用 kotlinPoet 生成 NavRegistry.kt 文件, 保存到 annotation 模块下以让 app 模块访问
    // 注意: KotlinPoet 生成的是源代码而不是字节码
    private fun generateNavRegistryClass() {
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
            .addCode(CodeBlock.builder()
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
        check(annotationProject != null){ throw GradleException("NullPointerException: annotation 模块不存在!") }
        val sourceSets = annotationProject!!.extensions.findByName("sourceSets") as SourceSetContainer
        val outputDir: File = sourceSets.first().java.srcDirs.first().absoluteFile
        fileSpec.writeTo(outputDir)

        println("3.代码生成: ${outputDir}")
        println("------------------------------------------------------------")
//        fileSpec.writeTo(System.out) // 测试-打印到控制台
    }
}