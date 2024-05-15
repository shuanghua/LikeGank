// 该类是自动生成的, 请勿手动修改!
package dev.shuanghua.annotations

import dev.shuanghua.annotations.NavDestination.NavType.Activity
import dev.shuanghua.annotations.NavDestination.NavType.Dialog
import dev.shuanghua.annotations.NavDestination.NavType.Fragment
import dev.shuanghua.annotations.NavDestination.NavType.None
import kotlin.collections.ArrayList
import kotlin.collections.List

public object NavRegistry {
  private val navList: ArrayList<NavData> = ArrayList()


  init {
    navList.add(NavData("home_fragment", "com.shua.likegank.ui.HomeFragment", Fragment))

  }

  public fun `get`(): List<NavData> {
    val list = ArrayList<NavData>()
        list.addAll(navList)
        return list
  }
}
