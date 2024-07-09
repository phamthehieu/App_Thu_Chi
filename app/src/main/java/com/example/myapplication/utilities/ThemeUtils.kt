package com.example.myapplication.utilities

import android.app.Activity
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.example.myapplication.R

object ThemeUtils {
    fun checkCurrentTheme(activity: Activity) {
        val currentNightMode = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.black1)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

            }
        }
    }
}