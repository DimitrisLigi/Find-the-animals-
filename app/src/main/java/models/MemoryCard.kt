package models

data class MemoryCard(
    val identifier: Int,
    var faceUp: Boolean = false,
    var isMatched: Boolean = false
)
