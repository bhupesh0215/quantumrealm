package com.colorcascade.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var maxLife: Float,
    val color: Int,
    val size: Float
)

class ParticleSystem {
    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    fun addExplosion(x: Float, y: Float, color: Int, count: Int = 20) {
        repeat(count) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = Random.nextFloat() * 300 + 100
            val life = Random.nextFloat() * 1.5f + 0.5f
            
            particles.add(Particle(
                x = x,
                y = y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                life = life,
                maxLife = life,
                color = color,
                size = Random.nextFloat() * 8 + 4
            ))
        }
    }
    
    fun addTrail(x: Float, y: Float, color: Int) {
        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val speed = Random.nextFloat() * 50 + 25
        val life = Random.nextFloat() * 0.5f + 0.2f
        
        particles.add(Particle(
            x = x + Random.nextFloat() * 20 - 10,
            y = y + Random.nextFloat() * 20 - 10,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            life = life,
            maxLife = life,
            color = color,
            size = Random.nextFloat() * 4 + 2
        ))
    }
    
    fun update(deltaTime: Float) {
        particles.removeAll { particle ->
            particle.x += particle.vx * deltaTime
            particle.y += particle.vy * deltaTime
            particle.vy += 500 * deltaTime // gravity
            particle.life -= deltaTime
            particle.life <= 0
        }
    }
    
    fun draw(canvas: Canvas) {
        for (particle in particles) {
            val alpha = (particle.life / particle.maxLife * 255).toInt().coerceIn(0, 255)
            paint.color = Color.argb(alpha, Color.red(particle.color), Color.green(particle.color), Color.blue(particle.color))
            canvas.drawCircle(particle.x, particle.y, particle.size, paint)
        }
    }
    
    fun clear() {
        particles.clear()
    }
}
