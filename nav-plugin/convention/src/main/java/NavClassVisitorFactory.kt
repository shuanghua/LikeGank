import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

abstract class NavClassVisitorFactory :
    AsmClassVisitorFactory<InstrumentationParameters.None> {

    /**
     * 如果需要转换多个类,请使用 when 表达式返回对应的 ClassVisitor
     * 对应的 ClassVisitor 请自行实现
     */
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        // 访问类上面的注解
        return FragmentAnnotationVisitor(
            Opcodes.ASM9,
            nextClassVisitor
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        // 类字节码转换开关, 也就是判断是否需要转换该类
//        return classData.className.contains("SomeSource")
        return true
    }
}