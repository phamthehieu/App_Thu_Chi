package com.example.myapplication.utilities

import android.graphics.Color
import java.util.Random

object ColorUtils {

    fun generateRandomLightColors(size: Int): List<Int> {
        val colors = mutableSetOf<Int>()
        while (colors.size < size) {
            val newColor = generateRandomLightColor()
            if (!colors.contains(newColor)) {
                colors.add(newColor)
            }
        }
        return colors.toList()
    }

    private fun generateRandomLightColor(): Int {
        val random = Random()
        val baseColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        val lightColor = blendWithWhite(baseColor, 0.5f)
        return if (lightColor == Color.WHITE || lightColor == Color.BLACK) generateRandomLightColor() else lightColor
    }

    private fun blendWithWhite(color: Int, ratio: Float): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        val blendedRed = (red + (255 - red) * ratio).toInt()
        val blendedGreen = (green + (255 - green) * ratio).toInt()
        val blendedBlue = (blue + (255 - blue) * ratio).toInt()

        return Color.argb(255, blendedRed, blendedGreen, blendedBlue)
    }
}