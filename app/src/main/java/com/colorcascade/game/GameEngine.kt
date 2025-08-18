package com.colorcascade.game

import kotlin.random.Random
import kotlin.math.*

class GameEngine(private val gameView: GameView) {
    
    private val blocks = mutableListOf<Block>()
    private val particles = ParticleSystem()
    private val comboSystem = ComboSystem()
    
    private var gravity = 3f
    private val dropSpeed = 2f
    private var currentBlock: Block? = null
    private var nextBlock: Block? = null
    private var score = 0
    private var gameOver = false
    private var level = 1
    private var blocksCleared = 0
    
    // Game enhancement variables
    private var screenShake = 0f
    private var backgroundHue = 0f
    private var gameTime = 0f
    private var powerUpCharge = 0f
    private val maxPowerUpCharge = 100f
    
    // Game grid properties
    private val gridWidth = 7
    private val gridHeight = 12
    private val blockSize = 60f
    
    init {
        generateNextBlock()
        spawnNewBlock()
    }
    
    fun update(deltaTime: Float = 1f/60f) {
        if (gameOver) return
        
        gameTime += deltaTime
        backgroundHue = (backgroundHue + deltaTime * 10) % 360f
        
        // Update screen shake
        if (screenShake > 0) {
            screenShake -= deltaTime * 10f
            if (screenShake < 0) screenShake = 0f
        }
        
        // Update combo system
        comboSystem.update(deltaTime)
        
        // Update particles
        particles.update(deltaTime)
        
        // Update all blocks animation
        for (block in blocks) {
            block.pulsePhase += deltaTime * 3f
            block.rotationAngle += deltaTime * 30f
            block.timeAlive += deltaTime
            
            // Add trail particles for special blocks
            if (block.type != BlockType.NORMAL && Random.nextFloat() < 0.1f) {
                particles.addTrail(block.x, block.y, block.color)
            }
        }
        
        // Update current falling block
        currentBlock?.let { block ->
            block.y += dropSpeed + (level * 0.5f) // Increase speed per level
            block.pulsePhase += deltaTime * 5f
            block.timeAlive += deltaTime
            
            // Add falling trail for special blocks
            if (block.type != BlockType.NORMAL) {
                particles.addTrail(block.x, block.y, block.color)
            }
            
            // Check collision with bottom or other blocks
            if (checkCollision(block)) {
                // Place the block
                block.y -= dropSpeed + (level * 0.5f)
                blocks.add(block)
                
                // Apply special block effects on landing
                applyBlockEffects(block)
                
                // Check for merges and cascades
                val mergedCount = checkForMerges()
                if (mergedCount > 0) {
                    screenShake = 5f
                    powerUpCharge += mergedCount * 2f
                    if (powerUpCharge > maxPowerUpCharge) powerUpCharge = maxPowerUpCharge
                }
                
                // Spawn new block
                spawnNewBlock()
                
                // Check game over
                if (block.y <= blockSize) {
                    gameOver = true
                    particles.addExplosion(gameView.width / 2f, gameView.height / 2f, android.graphics.Color.RED, 50)
                }
            }
        }
        
        // Apply gravity to all blocks
        applyGravity()
        
        // Check level progression
        checkLevelProgression()
    }
    
    
    private fun generateNextBlock(): Block {
        val centerX = gameView.width / 2f
        val blockType = Block.getRandomBlockType()
        val normalColor = Block.getRandomColor()
        val color = Block.getColorForType(blockType, normalColor)
        
        return Block(
            x = centerX,
            y = blockSize / 2,
            color = color,
            size = blockSize,
            type = blockType
        )
    }
    
    private fun spawnNewBlock() {
        currentBlock = nextBlock ?: generateNextBlock()
        nextBlock = generateNextBlock()
        
        // Reset position
        currentBlock?.let {
            it.x = gameView.width / 2f
            it.y = blockSize / 2
            it.pulsePhase = 0f
            it.rotationAngle = 0f
            it.timeAlive = 0f
        }
    }
    
    private fun applyBlockEffects(block: Block) {
        when (block.type) {
            BlockType.BOMB -> {
                // Explode nearby blocks
                val nearbyBlocks = blocks.filter { other ->
                    val distance = sqrt((block.x - other.x).pow(2) + (block.y - other.y).pow(2))
                    distance <= blockSize * 2 && other != block
                }
                
                for (nearby in nearbyBlocks) {
                    particles.addExplosion(nearby.x, nearby.y, nearby.color, 15)
                    blocks.remove(nearby)
                    val points = comboSystem.addCombo(25)
                    score += points
                    blocksCleared++
                }
                
                // Remove the bomb itself
                particles.addExplosion(block.x, block.y, block.color, 25)
                blocks.remove(block)
            }
            
            BlockType.GRAVITY -> {
                // Increase gravity temporarily
                gravity = 8f
                // Reset gravity after 3 seconds (handled elsewhere)
            }
            
            BlockType.TRANSFORMER -> {
                // Change color of random blocks
                val randomBlocks = blocks.shuffled().take(3)
                for (randomBlock in randomBlocks) {
                    if (randomBlock != block) {
                        randomBlock.color = block.color
                        particles.addExplosion(randomBlock.x, randomBlock.y, randomBlock.color, 5)
                    }
                }
            }
            
            else -> {} // Normal blocks don't have special effects
        }
    }
    
    fun moveBlockLeft() {
        currentBlock?.let { block ->
            val newX = block.x - blockSize
            if (newX - blockSize/2 >= 0 && !checkHorizontalCollision(block, newX)) {
                block.x = newX
            }
        }
    }
    
    fun moveBlockRight() {
        currentBlock?.let { block ->
            val newX = block.x + blockSize
            if (newX + blockSize/2 <= gameView.width && !checkHorizontalCollision(block, newX)) {
                block.x = newX
            }
        }
    }
    
    fun dropBlock() {
        currentBlock?.let { block ->
            while (!checkCollision(block)) {
                block.y += dropSpeed
            }
            block.y -= dropSpeed // Move back to last valid position
        }
    }
    
    private fun checkCollision(block: Block): Boolean {
        // Check bottom boundary
        if (block.y + blockSize/2 >= gameView.height - 100) { // Leave space for UI
            return true
        }
        
        // Check collision with other blocks
        return blocks.any { it.intersects(block) }
    }
    
    private fun checkHorizontalCollision(block: Block, newX: Float): Boolean {
        val testBlock = Block(newX, block.y, block.color, block.size)
        return blocks.any { it.intersects(testBlock) }
    }
    
    private fun applyGravity() {
        var anyMoved = true
        while (anyMoved) {
            anyMoved = false
            for (block in blocks) {
                val newY = block.y + gravity
                if (newY + blockSize/2 < gameView.height - 100 && 
                    !blocks.any { other -> other != block && 
                        Block(block.x, newY, block.color, block.size).intersects(other) }) {
                    block.y = newY
                    anyMoved = true
                }
            }
        }
    }
    
    private fun checkForMerges(): Int {
        val toRemove = mutableSetOf<Block>()
        val toAdd = mutableListOf<Block>()
        var totalMerged = 0
        
        for (i in blocks.indices) {
            if (blocks[i] in toRemove) continue
            
            val cluster = findConnectedBlocks(blocks[i], mutableSetOf())
            if (cluster.size >= 3) {
                // Calculate merge position (center of cluster)
                val centerX = cluster.map { it.x }.average().toFloat()
                val centerY = cluster.map { it.y }.average().toFloat()
                
                // Create explosion effects
                for (block in cluster) {
                    particles.addExplosion(block.x, block.y, block.color, 8)
                }
                
                // Create bigger merged block with special properties
                val mergedSize = minOf(blockSize + cluster.size * 3f, blockSize * 1.5f)
                val mergedType = if (cluster.size >= 5) BlockType.BOMB else BlockType.NORMAL
                val mergedColor = if (mergedType == BlockType.BOMB) 
                    Block.getColorForType(BlockType.BOMB, blocks[i].color) 
                else blocks[i].color
                
                val mergedBlock = Block(
                    x = centerX,
                    y = centerY,
                    color = mergedColor,
                    size = mergedSize,
                    type = mergedType
                )
                
                toRemove.addAll(cluster)
                toAdd.add(mergedBlock)
                
                // Calculate points with combo system
                val basePoints = cluster.size * 10 + (cluster.size - 3) * 5
                val finalPoints = when {
                    cluster.any { it.type == BlockType.MULTIPLIER } -> basePoints * 2
                    cluster.any { it.type == BlockType.RAINBOW } -> basePoints + 50
                    else -> basePoints
                }
                
                val comboPoints = comboSystem.addCombo(finalPoints)
                score += comboPoints
                blocksCleared += cluster.size
                totalMerged += cluster.size
                
                // Add big explosion for large merges
                if (cluster.size >= 5) {
                    particles.addExplosion(centerX, centerY, mergedColor, 30)
                    screenShake = 8f
                }
            }
        }
        
        // Apply changes
        blocks.removeAll(toRemove)
        blocks.addAll(toAdd)
        
        return totalMerged
    }
    
    private fun findConnectedBlocks(block: Block, visited: MutableSet<Block>): Set<Block> {
        if (block in visited) return emptySet()
        
        visited.add(block)
        val connected = mutableSetOf(block)
        
        for (other in blocks) {
            if (other != block && other !in visited) {
                val distance = sqrt((block.x - other.x).pow(2) + (block.y - other.y).pow(2))
                val colorMatch = block.color == other.color || 
                               block.type == BlockType.RAINBOW || 
                               other.type == BlockType.RAINBOW
                
                if (distance <= blockSize && colorMatch) {
                    connected.addAll(findConnectedBlocks(other, visited))
                }
            }
        }
        
        return connected
    }
    
    private fun checkLevelProgression() {
        val newLevel = (blocksCleared / 50) + 1
        if (newLevel > level) {
            level = newLevel
            particles.addExplosion(gameView.width / 2f, gameView.height / 4f, android.graphics.Color.YELLOW, 40)
            screenShake = 10f
            
            // Increase difficulty
            gravity = 3f + level * 0.5f
        }
    }
    
    fun restart() {
        blocks.clear()
        particles.clear()
        comboSystem.resetCombo()
        score = 0
        level = 1
        blocksCleared = 0
        gameOver = false
        gravity = 3f
        screenShake = 0f
        gameTime = 0f
        powerUpCharge = 0f
        generateNextBlock()
        spawnNewBlock()
    }
    
    fun usePowerUp() {
        if (powerUpCharge >= maxPowerUpCharge) {
            powerUpCharge = 0f
            
            // Clear bottom 2 rows
            val bottomBlocks = blocks.filter { it.y > gameView.height - 200 }
            for (block in bottomBlocks) {
                particles.addExplosion(block.x, block.y, block.color, 10)
                score += 50
            }
            blocks.removeAll(bottomBlocks)
            
            screenShake = 15f
        }
    }
    
    fun activateSlowMotion() {
        // This could be called from GameView for special effects
        gravity = 1f
        // Reset after some time
    }
    
    // Getters with new properties
    fun getBlocks(): List<Block> = blocks
    fun getCurrentBlock(): Block? = currentBlock
    fun getNextBlock(): Block? = nextBlock
    fun getScore(): Int = score
    fun isGameOver(): Boolean = gameOver
    fun getLevel(): Int = level
    fun getComboSystem(): ComboSystem = comboSystem
    fun getParticleSystem(): ParticleSystem = particles
    fun getScreenShake(): Float = screenShake
    fun getBackgroundHue(): Float = backgroundHue
    fun getPowerUpCharge(): Float = powerUpCharge
    fun getMaxPowerUpCharge(): Float = maxPowerUpCharge
    fun canUsePowerUp(): Boolean = powerUpCharge >= maxPowerUpCharge
}
