import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor

class FragmentAnnotationVisitor(
    api: Int,
    cv: ClassVisitor
) : ClassVisitor(api, cv) {

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        println("1.Found @NavDestination on a Fragment class")
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        if (descriptor == "Ldev/shuanghua/annotations/NavDestination;") {
            // 处理找到的注解信息
            println("Found @NavDestination on a Fragment class")
        }
        return super.visitAnnotation(descriptor, visible)
    }
}
