package com.colorcascade.game

import kotlin.random.Random

class GameEngine(private val gameView: GameView) {
    
    private val blocks = mutableListOf<Block>()
    private val gravity = 3f
    private val dropSpeed = 2f
    private var currentBlock: Block? = null
    private var nextBlock: Block? = null
    private var score = 0
    private var gameOver = false
    
    // Game grid properties
    private val gridWidth = 7
    private val gridHeight = 12
    private val blockSize = 60f
    
    init {
        generateNextBlock()
        spawnNewBlock()
    }
    
    fun update() {
        if (gameOver) return
        
        // Update current falling block
        currentBlock?.let { block ->
            block.y += dropSpeed
            
            // Check collision with bottom or other blocks
            if (checkCollision(block)) {
                // Place the block
                block.y -= dropSpeed // Move back to last valid position
                blocks.add(block)
                
                // Check for merges and cascades
                checkForMerges()
                
                // Spawn new block
                spawnNewBlock()
                
                // Check game over
                if (block.y <= blockSize) {
                    gameOver = true
                }
            }
        }
        
        // Apply gravity to all blocks
        applyGravity()
    }
    
    private fun generateNextBlock(): Block {
        val centerX = gameView.width / 2f
        return Block(
            x = centerX,
            y = blockSize / 2,
            color = Block.getRandomColor(),
            size = blockSize
        )
    }
    
    private fun spawnNewBlock() {
        currentBlock = nextBlock ?: generateNextBlock()
        nextBlock = generateNextBlock()
        
        // Reset position
        currentBlock?.let {
            it.x = gameView.width / 2f
            it.y = blockSize / 2
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
    
    private fun checkForMerges() {
        val toRemove = mutableSetOf<Block>()
        val toAdd = mutableListOf<Block>()
        
        for (i in blocks.indices) {
            if (blocks[i] in toRemove) continue
            
            val cluster = findConnectedBlocks(blocks[i], mutableSetOf())
            if (cluster.size >= 3) {
                // Calculate merge position (center of cluster)
                val centerX = cluster.map { it.x }.average().toFloat()
                val centerY = cluster.map { it.y }.average().toFloat()
                
                // Create new merged block (same color, slightly larger)
                val mergedBlock = Block(
                    x = centerX,
                    y = centerY,
                    color = blocks[i].color,
                    size = blockSize + 10f
                )
                
                toRemove.addAll(cluster)
                toAdd.add(mergedBlock)
                
                // Add score
                score += cluster.size * 10
            }
        }
        
        // Apply changes
        blocks.removeAll(toRemove)
        blocks.addAll(toAdd)
    }
    
    private fun findConnectedBlocks(block: Block, visited: MutableSet<Block>): Set<Block> {
        if (block in visited) return emptySet()
        
        visited.add(block)
        val connected = mutableSetOf(block)
        
        for (other in blocks) {
            if (other != block && other !in visited && block.isTouching(other)) {
                connected.addAll(findConnectedBlocks(other, visited))
            }
        }
        
        return connected
    }
    
    fun restart() {
        blocks.clear()
        score = 0
        gameOver = false
        generateNextBlock()
        spawnNewBlock()
    }
    
    // Getters
    fun getBlocks(): List<Block> = blocks
    fun getCurrentBlock(): Block? = currentBlock
    fun getNextBlock(): Block? = nextBlock
    fun getScore(): Int = score
    fun isGameOver(): Boolean = gameOver
}
