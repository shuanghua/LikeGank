import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

abstract class AsmClassesVisitorFactory :
    AsmClassVisitorFactory<AsmClassesVisitorFactory.Params> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return AsmModifyClassesVisitor(
            Opcodes.ASM9,
            nextClassVisitor,
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return classData.className.contains("BaseFragment")
    }

    interface Params : InstrumentationParameters {
    }
}