package com.colorcascade.game

import kotlin.math.pow

class ComboSystem {
    private var currentCombo = 0
    private var comboTimer = 0f
    private val comboTimeLimit = 3f // 3 seconds to maintain combo
    private var totalScore = 0
    
    fun addCombo(basePoints: Int): Int {
        currentCombo++
        comboTimer = comboTimeLimit
        
        // Calculate multiplier: 1x, 1.5x, 2x, 2.5x, 3x, etc.
        val multiplier = 1f + (currentCombo - 1) * 0.5f
        val points = (basePoints * multiplier).toInt()
        totalScore += points
        
        return points
    }
    
    fun update(deltaTime: Float) {
        if (currentCombo > 0) {
            comboTimer -= deltaTime
            if (comboTimer <= 0) {
                resetCombo()
            }
        }
    }
    
    fun resetCombo() {
        currentCombo = 0
        comboTimer = 0f
    }
    
    fun getCurrentCombo(): Int = currentCombo
    fun getComboMultiplier(): Float = 1f + (currentCombo - 1) * 0.5f
    fun getTotalScore(): Int = totalScore
    fun isComboActive(): Boolean = currentCombo > 0
    fun getComboTimeLeft(): Float = comboTimer
    fun getComboTimeLimit(): Float = comboTimeLimit
}
