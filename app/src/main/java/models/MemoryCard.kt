package models

data class MemoryCard(
    val identifier: Int,
    val imageUrl: String? = null,
    var faceUp: Boolean = false,
    var isMatched: Boolean = false
)
