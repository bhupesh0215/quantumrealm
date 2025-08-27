package com.colorcascade.game

import android.graphics.Color
import kotlin.random.Random

enum class BlockType {
    NORMAL,
    BOMB,          // Explodes nearby blocks
    RAINBOW,       // Matches any color
    MULTIPLIER,    // Doubles points
    GRAVITY,       // Pulls blocks down faster
    TRANSFORMER    // Changes color on impact
}

data class Block(
    var x: Float,
    var y: Float,
    var color: Int,
    val size: Float = 60f,
    var isActive: Boolean = true,
    val type: BlockType = BlockType.NORMAL,
    var pulsePhase: Float = 0f,  // For animation
    var rotationAngle: Float = 0f, // For rotation effects
    var timeAlive: Float = 0f    // For special effects timing
) {
    companion object {
        private val colors = arrayOf(
            Color.parseColor("#FF6B6B"), // Red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#45B7D1"), // Blue
            Color.parseColor("#FFA726"), // Orange
            Color.parseColor("#66BB6A"), // Green
            Color.parseColor("#AB47BC"), // Purple
            Color.parseColor("#FFC107"), // Yellow
            Color.parseColor("#E91E63")  // Pink
        )
        
        private val specialColors = mapOf(
            BlockType.BOMB to Color.parseColor("#FF3030"),
            BlockType.RAINBOW to Color.parseColor("#FFD700"),
            BlockType.MULTIPLIER to Color.parseColor("#9C27B0"),
            BlockType.GRAVITY to Color.parseColor("#424242"),
            BlockType.TRANSFORMER to Color.parseColor("#00BCD4")
        )
        
        fun getRandomColor(): Int = colors[Random.nextInt(colors.size)]
        
        fun getRandomBlockType(): BlockType {
            val rand = Random.nextFloat()
            return when {
                rand < 0.75f -> BlockType.NORMAL
                rand < 0.85f -> BlockType.BOMB
                rand < 0.92f -> BlockType.RAINBOW
                rand < 0.96f -> BlockType.MULTIPLIER
                rand < 0.98f -> BlockType.GRAVITY
                else -> BlockType.TRANSFORMER
            }
        }
        
        fun getColorForType(type: BlockType, normalColor: Int): Int {
            return specialColors[type] ?: normalColor
        }
        
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
        return distance <= size && (color == other.color || type == BlockType.RAINBOW || other.type == BlockType.RAINBOW)
    }
    
    fun getDistanceTo(other: Block): Float {
        return kotlin.math.sqrt(
            ((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)).toDouble()
        ).toFloat()
    }
    
    fun isSpecial(): Boolean = type != BlockType.NORMAL
    
    fun getEffectiveColor(): Int {
        return if (type == BlockType.RAINBOW) {
            // Rainbow blocks can match any color
            Color.WHITE
        } else {
            color
        }
    }
}
