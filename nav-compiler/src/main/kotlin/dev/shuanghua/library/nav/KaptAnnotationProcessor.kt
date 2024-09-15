package dev.shuanghua.library.nav

import com.google.auto.service.AutoService
import dev.shuanghua.annotations.NavData
import dev.shuanghua.annotations.NavDestination
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 当前代码是 kapt 实现代码生成，而不是 ksp
 * kapt 负责识别注解，提取注解信息
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("dev.shuanghua.annotations.NavDestination")
class KaptAnnotationProcessor : AbstractProcessor() {
    private val navDataList = mutableSetOf<NavData>()
    override fun process( // 注意: 此函数会执行多次
        elements: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val fragmentsWithAnnotation = roundEnv.getElementsAnnotatedWith(NavDestination::class.java)
        fragmentsWithAnnotation.forEach { element ->
            val annotation = element.getAnnotation(NavDestination::class.java)
            val type = annotation.type
            val route = annotation.route
            val packageName = processingEnv.elementUtils.getPackageOf(element).qualifiedName
            val className = element.simpleName
            val classFullName = "$packageName.$className"
            navDataList.add(NavData(route = route, type = type, className = classFullName))
        }
//        val kaptKotlinGeneratedDir: String? = processingEnv.options["kapt.kotlin.generated"]
        if (navDataList.isNotEmpty()) {
            KotlinpoetNavRegistryClass(
                outputDirectory = processingEnv.filer,
                navDataList = navDataList
            ).also { it.generateNavRegistryClass() }
        }
        return true
    }
}