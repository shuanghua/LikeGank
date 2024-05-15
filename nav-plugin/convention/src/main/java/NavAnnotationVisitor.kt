import dev.shuanghua.annotations.NavData
import dev.shuanghua.annotations.NavDestination
import org.gradle.api.GradleException
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode

/**
 * 解析注解信息
 */
class NavAnnotationVisitor(
    private val classReader: ClassReader,
    private val navDataList: MutableList<NavData>
) : ClassVisitor(Opcodes.ASM9) {

    companion object {
        const val nav_annotation_destination = "Ldev/shuanghua/annotations/NavDestination;"
        private const val key_route = "route"
        private const val key_type = "type"
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {

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
//                println("注解解析: $navDataList")
            }
        }
    }
}