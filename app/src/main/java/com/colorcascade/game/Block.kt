package com.colorcascade.game

import android.graphics.Color
import kotlin.random.Random

data class Block(
    var x: Float,
    var y: Float,
    val color: Int,
    val size: Float = 60f,
    var isActive: Boolean = true
) {
    companion object {
        private val colors = arrayOf(
            Color.parseColor("#FF6B6B"), // Red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#45B7D1"), // Blue
            Color.parseColor("#FFA726"), // Orange
            Color.parseColor("#66BB6A"), // Green
            Color.parseColor("#AB47BC")  // Purple
        )
        
        fun getRandomColor(): Int = colors[Random.nextInt(colors.size)]
        
        fun getColors(): Array<Int> = colors
    }
    
    fun getLeft(): Float = x - size / 2
    fun getRight(): Float = x + size / 2
    fun getTop(): Float = y - size / 2
    fun getBottom(): Float = y + size / 2
    
    fun intersects(other: Block): Boolean {
        return getLeft() < other.getRight() &&
                getRight() > other.getLeft() &&
                getTop() < other.getBottom() &&
                getBottom() > other.getTop()
    }
    
    fun isTouching(other: Block): Boolean {
        val distance = kotlin.math.sqrt(
            ((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)).toDouble()
        ).toFloat()
        return distance <= size && color == other.color
    }
}
