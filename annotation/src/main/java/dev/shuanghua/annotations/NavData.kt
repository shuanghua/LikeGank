package dev.shuanghua.annotations

data class NavData(
    val route: String,
    val className: String,
    val type: NavDestination.NavType
)
