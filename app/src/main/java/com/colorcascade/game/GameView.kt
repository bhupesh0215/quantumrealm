package com.colorcascade.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private var gameThread: GameThread? = null
    private lateinit var gameEngine: GameEngine
    
    // Enhanced paint objects
    private val blockPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }
    
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#2C3E50")
    }
    
    private val gradientPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    // Touch handling
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var touchStartTime = 0L
    private var isLongPress = false
    
    // Visual effects
    private var backgroundOffset = 0f
    private val stars = mutableListOf<Star>()
    
    data class Star(var x: Float, var y: Float, var brightness: Float, var speed: Float)
    
    init {
        holder.addCallback(this)
        isFocusable = true
        
        // Generate background stars
        repeat(50) {
            stars.add(Star(
                x = Random.nextFloat() * 1000,
                y = Random.nextFloat() * 2000,
                brightness = Random.nextFloat(),
                speed = Random.nextFloat() * 50 + 25
            ))
        }
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        gameEngine = GameEngine(this)
        gameThread = GameThread(holder)
        gameThread?.start()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes if needed
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameThread?.stopGame()
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                touchStartTime = System.currentTimeMillis()
                isLongPress = false
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                val duration = System.currentTimeMillis() - touchStartTime
                if (duration > 500) { // Long press for slow motion
                    isLongPress = true
                    gameEngine.activateSlowMotion()
                }
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                val touchDuration = System.currentTimeMillis() - touchStartTime
                val deltaX = event.x - lastTouchX
                val deltaY = event.y - lastTouchY
                
                when {
                    // Check power-up button area (top-right corner)
                    event.x > width - 120f && event.y < 120f && gameEngine.canUsePowerUp() -> {
                        gameEngine.usePowerUp()
                    }
                    // Long press was for slow motion
                    isLongPress -> {
                        // Reset gravity back to normal
                    }
                    // Quick tap - drop block fast
                    touchDuration < 200 && kotlin.math.abs(deltaX) < 50 && kotlin.math.abs(deltaY) < 50 -> {
                        gameEngine.dropBlock()
                    }
                    // Swipe left
                    deltaX < -100 && kotlin.math.abs(deltaY) < 100 -> {
                        gameEngine.moveBlockLeft()
                    }
                    // Swipe right
                    deltaX > 100 && kotlin.math.abs(deltaY) < 100 -> {
                        gameEngine.moveBlockRight()
                    }
                    // Swipe up for instant drop
                    deltaY < -150 && kotlin.math.abs(deltaX) < 100 -> {
                        gameEngine.dropBlock()
                    }
                    // Restart game if game over
                    gameEngine.isGameOver() -> {
                        gameEngine.restart()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    fun update() {
        gameEngine.update()
        
        // Update background effects
        backgroundOffset += 2f
        if (backgroundOffset > height) backgroundOffset = 0f
        
        // Update stars
        for (star in stars) {
            star.y += star.speed * (1f/60f)
            if (star.y > height) {
                star.y = -10f
                star.x = Random.nextFloat() * width
            }
            star.brightness = 0.3f + sin(star.y * 0.01f) * 0.3f
        }
    }
    
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        
        // Apply screen shake
        val shakeAmount = gameEngine.getScreenShake()
        if (shakeAmount > 0) {
            val shakeX = (Random.nextFloat() - 0.5f) * shakeAmount
            val shakeY = (Random.nextFloat() - 0.5f) * shakeAmount
            canvas.translate(shakeX, shakeY)
        }
        
        // Draw dynamic background
        drawDynamicBackground(canvas)
        
        // Draw grid with glow effect
        drawEnhancedGrid(canvas)
        
        // Draw placed blocks with effects
        for (block in gameEngine.getBlocks()) {
            drawEnhancedBlock(canvas, block)
        }
        
        // Draw current falling block with trail
        gameEngine.getCurrentBlock()?.let { block ->
            drawEnhancedBlock(canvas, block, isCurrentBlock = true)
        }
        
        // Draw particle effects
        gameEngine.getParticleSystem().draw(canvas)
        
        // Draw enhanced UI
        drawEnhancedUI(canvas)
        
        // Draw combo effects
        drawComboEffects(canvas)
        
        // Draw game over screen
        if (gameEngine.isGameOver()) {
            drawEnhancedGameOver(canvas)
        }
    }
    
    private fun drawDynamicBackground(canvas: Canvas) {
        // Create gradient background that changes with level
        val hue = gameEngine.getBackgroundHue()
        val colors = intArrayOf(
            Color.HSVToColor(floatArrayOf(hue, 0.8f, 0.3f)),
            Color.HSVToColor(floatArrayOf((hue + 60) % 360, 0.6f, 0.2f)),
            Color.HSVToColor(floatArrayOf((hue + 120) % 360, 0.4f, 0.1f))
        )
        
        val gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            colors, null, Shader.TileMode.CLAMP
        )
        gradientPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)
        
        // Draw moving stars
        val starPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        
        for (star in stars) {
            starPaint.alpha = (star.brightness * 255).toInt()
            canvas.drawCircle(star.x, star.y, star.brightness * 3f, starPaint)
        }
    }
    
    private fun drawEnhancedGrid(canvas: Canvas) {
        val gridPaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        }
        
        val blockSize = 60f
        val cols = width / blockSize.toInt()
        val rows = (height - 150) / blockSize.toInt()
        
        // Draw glowing grid lines
        for (i in 0..cols) {
            val x = i * blockSize
            canvas.drawLine(x, 0f, x, height - 150f, gridPaint)
        }
        
        for (i in 0..rows) {
            val y = i * blockSize
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }
    }
    
    private fun drawEnhancedBlock(canvas: Canvas, block: Block, isCurrentBlock: Boolean = false) {
        val left = block.x - block.size / 2
        val top = block.y - block.size / 2
        val right = block.x + block.size / 2
        val bottom = block.y + block.size / 2
        
        // Draw glow effect for special blocks
        if (block.type != BlockType.NORMAL) {
            val glowRadius = block.size / 2 + sin(block.pulsePhase) * 10f
            val glowGradient = RadialGradient(
                block.x, block.y, glowRadius,
                Color.argb(100, Color.red(block.color), Color.green(block.color), Color.blue(block.color)),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            glowPaint.shader = glowGradient
            canvas.drawCircle(block.x, block.y, glowRadius, glowPaint)
        }
        
        // Main block color with pulse effect
        val pulseIntensity = if (isCurrentBlock) 0.3f else 0.1f
        val colorIntensity = 1f + sin(block.pulsePhase) * pulseIntensity
        val red = (Color.red(block.color) * colorIntensity).toInt().coerceIn(0, 255)
        val green = (Color.green(block.color) * colorIntensity).toInt().coerceIn(0, 255)
        val blue = (Color.blue(block.color) * colorIntensity).toInt().coerceIn(0, 255)
        
        blockPaint.color = Color.rgb(red, green, blue)
        
        // Draw block with special shape based on type
        val rect = RectF(left, top, right, bottom)
        
        when (block.type) {
            BlockType.BOMB -> {
                // Draw spiky bomb shape
                canvas.save()
                canvas.rotate(block.rotationAngle, block.x, block.y)
                drawStar(canvas, block.x, block.y, block.size / 2, blockPaint)
                canvas.restore()
            }
            BlockType.RAINBOW -> {
                // Draw with rainbow gradient
                val rainbowColors = intArrayOf(
                    Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA
                )
                val rainbow = LinearGradient(
                    left, top, right, bottom,
                    rainbowColors, null, Shader.TileMode.CLAMP
                )
                blockPaint.shader = rainbow
                canvas.drawRoundRect(rect, 15f, 15f, blockPaint)
                blockPaint.shader = null
            }
            BlockType.MULTIPLIER -> {
                // Draw diamond shape
                canvas.save()
                canvas.rotate(45f + block.rotationAngle, block.x, block.y)
                canvas.drawRoundRect(rect, 8f, 8f, blockPaint)
                canvas.restore()
            }
            else -> {
                // Normal rounded rectangle
                canvas.drawRoundRect(rect, 12f, 12f, blockPaint)
            }
        }
        
        // Draw border with glow
        strokePaint.strokeWidth = if (isCurrentBlock) 4f else 2f
        strokePaint.color = Color.WHITE
        if (block.type != BlockType.NORMAL) {
            strokePaint.setShadowLayer(8f, 0f, 0f, Color.WHITE)
        }
        canvas.drawRoundRect(rect, 12f, 12f, strokePaint)
        strokePaint.clearShadowLayer()
        
        // Draw type indicator
        drawBlockTypeIndicator(canvas, block)
        
        // Add 3D highlight effect
        val highlightPaint = Paint().apply {
            color = Color.WHITE
            alpha = 120
        }
        canvas.drawRoundRect(
            RectF(left + 4, top + 4, right - 4, top + block.size / 4),
            6f, 6f, highlightPaint
        )
    }
    
    private fun drawStar(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, paint: Paint) {
        val path = Path()
        val angles = 8
        val outerRadius = radius
        val innerRadius = radius * 0.5f
        
        for (i in 0 until angles * 2) {
            val angle = i * PI.toFloat() / angles
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = centerX + cos(angle) * r
            val y = centerY + sin(angle) * r
            
            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
    
    private fun drawBlockTypeIndicator(canvas: Canvas, block: Block) {
        val iconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        
        val icon = when (block.type) {
            BlockType.BOMB -> "💣"
            BlockType.RAINBOW -> "🌈"
            BlockType.MULTIPLIER -> "×2"
            BlockType.GRAVITY -> "⬇"
            BlockType.TRANSFORMER -> "🔄"
            else -> return
        }
        
        canvas.drawText(icon, block.x, block.y + 8f, iconPaint)
    }
    
    private fun drawEnhancedUI(canvas: Canvas) {
        val uiArea = height - 150f
        
        // Draw UI background with gradient
        val uiGradient = LinearGradient(
            0f, uiArea, 0f, height.toFloat(),
            Color.argb(200, 26, 37, 47),
            Color.argb(255, 26, 37, 47),
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = uiGradient
        canvas.drawRect(0f, uiArea, width.toFloat(), height.toFloat(), gradientPaint)
        
        // Draw score with glow
        textPaint.setShadowLayer(8f, 0f, 0f, Color.YELLOW)
        textPaint.textSize = 36f
        canvas.drawText("Score: ${gameEngine.getScore()}", 20f, uiArea + 35f, textPaint)
        
        // Draw level
        textPaint.textSize = 28f
        canvas.drawText("Level: ${gameEngine.getLevel()}", 20f, uiArea + 70f, textPaint)
        textPaint.clearShadowLayer()
        
        // Draw power-up charge bar
        drawPowerUpBar(canvas, uiArea)
        
        // Draw next block preview with effects
        drawNextBlockPreview(canvas, uiArea)
        
        // Draw controls hint with animation
        val hintPaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 18f
            alpha = (128 + sin(System.currentTimeMillis() * 0.003f) * 127).toInt()
        }
        canvas.drawText("Swipe ← → ↑, Tap to drop, Hold for slow-mo", 20f, uiArea + 110f, hintPaint)
        canvas.drawText("Tap power-up button when charged!", 20f, uiArea + 130f, hintPaint)
    }
    
    private fun drawPowerUpBar(canvas: Canvas, uiArea: Float) {
        val barWidth = 200f
        val barHeight = 20f
        val barX = width - barWidth - 140f
        val barY = uiArea + 15f
        
        // Background
        val barBgPaint = Paint().apply {
            color = Color.GRAY
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(barX, barY, barX + barWidth, barY + barHeight), 10f, 10f, barBgPaint)
        
        // Fill based on charge
        val chargePercent = gameEngine.getPowerUpCharge() / gameEngine.getMaxPowerUpCharge()
        val fillWidth = barWidth * chargePercent
        
        val fillPaint = Paint().apply {
            color = if (gameEngine.canUsePowerUp()) Color.GREEN else Color.YELLOW
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(barX, barY, barX + fillWidth, barY + barHeight), 10f, 10f, fillPaint)
        
        // Power-up button
        val buttonSize = 40f
        val buttonX = width - buttonSize - 20f
        val buttonY = uiArea + 5f
        
        val buttonPaint = Paint().apply {
            color = if (gameEngine.canUsePowerUp()) Color.GREEN else Color.GRAY
            style = Paint.Style.FILL
        }
        
        if (gameEngine.canUsePowerUp()) {
            // Add glow effect
            buttonPaint.setShadowLayer(15f, 0f, 0f, Color.GREEN)
        }
        
        canvas.drawRoundRect(RectF(buttonX, buttonY, buttonX + buttonSize, buttonY + buttonSize), 8f, 8f, buttonPaint)
        buttonPaint.clearShadowLayer()
        
        // Power-up icon
        val iconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("⚡", buttonX + buttonSize/2, buttonY + buttonSize/2 + 8f, iconPaint)
    }
    
    private fun drawNextBlockPreview(canvas: Canvas, uiArea: Float) {
        gameEngine.getNextBlock()?.let { nextBlock ->
            val previewX = width - 100f
            val previewY = uiArea + 80f
            
            textPaint.textSize = 20f
            canvas.drawText("Next:", width - 130f, uiArea + 65f, textPaint)
            
            // Create a smaller version of the block for preview
            val previewBlock = Block(
                x = previewX,
                y = previewY,
                color = nextBlock.color,
                size = 30f,
                type = nextBlock.type,
                pulsePhase = nextBlock.pulsePhase
            )
            
            drawEnhancedBlock(canvas, previewBlock)
        }
    }
    
    private fun drawComboEffects(canvas: Canvas) {
        val combo = gameEngine.getComboSystem()
        if (combo.isComboActive()) {
            // Draw combo counter with animation
            val comboPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 48f + sin(System.currentTimeMillis() * 0.01f) * 8f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(10f, 0f, 0f, Color.RED)
            }
            
            canvas.drawText("COMBO x${combo.getCurrentCombo()}!", width / 2f, 100f, comboPaint)
            
            // Draw combo timer bar
            val timerWidth = 200f
            val timerHeight = 8f
            val timerX = width / 2f - timerWidth / 2f
            val timerY = 120f
            
            val timerProgress = combo.getComboTimeLeft() / combo.getComboTimeLimit()
            val progressWidth = timerWidth * timerProgress
            
            val timerBgPaint = Paint().apply { color = Color.GRAY }
            canvas.drawRoundRect(RectF(timerX, timerY, timerX + timerWidth, timerY + timerHeight), 4f, 4f, timerBgPaint)
            
            val timerFillPaint = Paint().apply { 
                color = if (timerProgress > 0.3f) Color.YELLOW else Color.RED 
            }
            canvas.drawRoundRect(RectF(timerX, timerY, timerX + progressWidth, timerY + timerHeight), 4f, 4f, timerFillPaint)
            
            comboPaint.clearShadowLayer()
        }
    }
    
    private fun drawEnhancedGameOver(canvas: Canvas) {
        // Animated overlay
        val overlayAlpha = (180 + sin(System.currentTimeMillis() * 0.002f) * 20).toInt()
        val overlayPaint = Paint().apply {
            color = Color.argb(overlayAlpha, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        
        // Game over text with animation
        val gameOverPaint = Paint().apply {
            color = Color.RED
            textSize = 64f + sin(System.currentTimeMillis() * 0.005f) * 8f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            setShadowLayer(15f, 0f, 0f, Color.YELLOW)
        }
        
        canvas.drawText("GAME OVER", width / 2f, height / 2f - 100f, gameOverPaint)
        
        // Final score with glow
        val finalScorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 42f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(8f, 0f, 0f, Color.BLUE)
        }
        
        canvas.drawText("Final Score: ${gameEngine.getScore()}", width / 2f, height / 2f - 20f, finalScorePaint)
        canvas.drawText("Level Reached: ${gameEngine.getLevel()}", width / 2f, height / 2f + 20f, finalScorePaint)
        
        // Restart instruction with pulse
        val restartPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 28f + sin(System.currentTimeMillis() * 0.008f) * 4f
            textAlign = Paint.Align.CENTER
            alpha = (200 + sin(System.currentTimeMillis() * 0.01f) * 55).toInt()
        }
        
        canvas.drawText("Tap anywhere to restart", width / 2f, height / 2f + 100f, restartPaint)
        
        // Add some celebration particles
        repeat(5) {
            val x = width / 2f + (Random.nextFloat() - 0.5f) * 200f
            val y = height / 2f + (Random.nextFloat() - 0.5f) * 100f
            val sparkle = Paint().apply {
                color = Color.YELLOW
                alpha = (Random.nextFloat() * 255).toInt()
            }
            canvas.drawCircle(x, y, Random.nextFloat() * 5f, sparkle)
        }
        
        gameOverPaint.clearShadowLayer()
        finalScorePaint.clearShadowLayer()
    }
    
    fun pause() {
        gameThread?.pause()
    }
    
    fun resume() {
        gameThread?.resumeGame()
    }
    
    private inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        private var running = false
        private var paused = false
        private val targetFPS = 60
        private val targetTime = 1000 / targetFPS
        private var lastTime = System.currentTimeMillis()
        
        fun stopGame() {
            running = false
        }
        
        fun pause() {
            paused = true
        }
        
        fun resumeGame() {
            paused = false
            lastTime = System.currentTimeMillis() // Reset time to avoid huge delta
        }
        
        override fun run() {
            running = true
            var startTime: Long
            var timeMillis: Long
            var waitTime: Long
            
            while (running) {
                if (!paused) {
                    startTime = System.nanoTime()
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastTime) / 1000f
                    lastTime = currentTime
                    
                    // Update game logic with proper delta time
                    update()
                    
                    // Draw everything
                    val canvas = surfaceHolder.lockCanvas()
                    canvas?.let {
                        try {
                            draw(it)
                        } catch (e: Exception) {
                            // Handle any drawing exceptions gracefully
                            e.printStackTrace()
                        } finally {
                            surfaceHolder.unlockCanvasAndPost(it)
                        }
                    }
                    
                    // Control frame rate
                    timeMillis = (System.nanoTime() - startTime) / 1000000
                    waitTime = targetTime - timeMillis
                    
                    if (waitTime > 0) {
                        try {
                            sleep(waitTime)
                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                } else {
                    try {
                        sleep(100)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        }
    }
}
