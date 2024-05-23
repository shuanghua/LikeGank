import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AsmModifyClassesVisitor (
    api: Int,
    cv: ClassVisitor,
) : ClassVisitor(api, cv) {

    private var className: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
        println("开始处理类：$name")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        println("准备插桩!!")
        val mv: MethodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        if (className == "com/shua/likegank/ui/base/BaseFragment" && (name == "onCreate" || name == "onDestroyView")) {
            // 在 onCreate() 方法和 onDestroyView() 方法中插入打印函数名的日志
            return PrintMethodNameAdapter(api, mv)
        }
        return mv
    }

    private class PrintMethodNameAdapter(
        api: Int,
        methodVisitor: MethodVisitor
    ) : MethodVisitor(api, methodVisitor) {
        override fun visitCode() {
            super.visitCode()
            println("开始插桩")
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
            )
            mv.visitLdcInsn("Method name: onCreate") // 或者 onDestroyView，具体方法名根据需要调整
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false
            )
        }
    }
}