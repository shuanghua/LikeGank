package dev.shuanghua.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class NavDestination(
    val type: NavType,
    val route: String
) {
    enum class NavType {
        Fragment,
        Activity,
        Dialog,
        None
    }
}