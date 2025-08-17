package com.colorcascade.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private var gameThread: GameThread? = null
    private lateinit var gameEngine: GameEngine
    
    // Paint objects for different elements
    private val blockPaint = Paint().apply {
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
    
    // Touch handling
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var touchStartTime = 0L
    
    init {
        holder.addCallback(this)
        isFocusable = true
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
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                val touchDuration = System.currentTimeMillis() - touchStartTime
                val deltaX = event.x - lastTouchX
                val deltaY = event.y - lastTouchY
                
                when {
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
    }
    
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        
        // Clear background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Draw grid lines
        drawGrid(canvas)
        
        // Draw placed blocks
        for (block in gameEngine.getBlocks()) {
            drawBlock(canvas, block)
        }
        
        // Draw current falling block
        gameEngine.getCurrentBlock()?.let { block ->
            drawBlock(canvas, block)
        }
        
        // Draw UI
        drawUI(canvas)
        
        // Draw game over screen
        if (gameEngine.isGameOver()) {
            drawGameOver(canvas)
        }
    }
    
    private fun drawGrid(canvas: Canvas) {
        val gridPaint = Paint().apply {
            color = Color.parseColor("#34495E")
            strokeWidth = 1f
        }
        
        val blockSize = 60f
        val cols = width / blockSize.toInt()
        val rows = (height - 100) / blockSize.toInt()
        
        // Draw vertical lines
        for (i in 0..cols) {
            val x = i * blockSize
            canvas.drawLine(x, 0f, x, height - 100f, gridPaint)
        }
        
        // Draw horizontal lines
        for (i in 0..rows) {
            val y = i * blockSize
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }
    }
    
    private fun drawBlock(canvas: Canvas, block: Block) {
        blockPaint.color = block.color
        
        val left = block.getLeft()
        val top = block.getTop()
        val right = block.getRight()
        val bottom = block.getBottom()
        
        // Draw block with rounded corners
        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, 8f, 8f, blockPaint)
        
        // Draw border
        canvas.drawRoundRect(rect, 8f, 8f, strokePaint)
        
        // Add highlight for 3D effect
        val highlightPaint = Paint().apply {
            color = Color.WHITE
            alpha = 80
        }
        canvas.drawRoundRect(
            RectF(left + 3, top + 3, right - 3, top + block.size / 4),
            4f, 4f, highlightPaint
        )
    }
    
    private fun drawUI(canvas: Canvas) {
        val uiArea = height - 100f
        
        // Draw UI background
        val uiPaint = Paint().apply {
            color = Color.parseColor("#1A252F")
        }
        canvas.drawRect(0f, uiArea, width.toFloat(), height.toFloat(), uiPaint)
        
        // Draw score
        canvas.drawText("Score: ${gameEngine.getScore()}", 20f, uiArea + 40f, textPaint)
        
        // Draw next block preview
        gameEngine.getNextBlock()?.let { nextBlock ->
            val previewX = width - 80f
            val previewY = uiArea + 50f
            
            textPaint.textSize = 24f
            canvas.drawText("Next:", width - 150f, uiArea + 25f, textPaint)
            textPaint.textSize = 48f
            
            blockPaint.color = nextBlock.color
            val previewRect = RectF(
                previewX - 20f, previewY - 20f,
                previewX + 20f, previewY + 20f
            )
            canvas.drawRoundRect(previewRect, 4f, 4f, blockPaint)
            canvas.drawRoundRect(previewRect, 4f, 4f, strokePaint)
        }
        
        // Draw controls hint
        val hintPaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 20f
        }
        canvas.drawText("Swipe ← → to move, Tap to drop", 20f, uiArea + 75f, hintPaint)
    }
    
    private fun drawGameOver(canvas: Canvas) {
        // Semi-transparent overlay
        val overlayPaint = Paint().apply {
            color = Color.BLACK
            alpha = 180
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        
        // Game over text
        val gameOverPaint = Paint().apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText("GAME OVER", width / 2f, height / 2f - 50f, gameOverPaint)
        
        val finalScorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText("Final Score: ${gameEngine.getScore()}", width / 2f, height / 2f + 20f, finalScorePaint)
        canvas.drawText("Tap to restart", width / 2f, height / 2f + 80f, finalScorePaint)
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
        
        fun stopGame() {
            running = false
        }
        
        fun pause() {
            paused = true
        }
        
        fun resumeGame() {
            paused = false
        }
        
        override fun run() {
            running = true
            var startTime: Long
            var timeMillis: Long
            var waitTime: Long
            
            while (running) {
                if (!paused) {
                    startTime = System.nanoTime()
                    
                    // Update game logic
                    update()
                    
                    // Draw everything
                    val canvas = surfaceHolder.lockCanvas()
                    canvas?.let {
                        draw(it)
                        surfaceHolder.unlockCanvasAndPost(it)
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
