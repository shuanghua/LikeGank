import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor

abstract class NavClassVisitorFactory :
    AsmClassVisitorFactory<InstrumentationParameters.None> {

//    override fun createClassVisitor(
//        classContext: ClassContext,
//        nextClassVisitor: ClassVisitor
//    ): ClassVisitor {
//        // 访问类上面的注解
//        return ClassAnnotationVisitor()
//    }
//
//    override fun isInstrumentable(classData: ClassData): Boolean {
////        return classData.className.contains("SomeSource")
//        return true
//    }
}