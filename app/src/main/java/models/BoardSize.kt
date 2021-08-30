package models

enum class BoardSize(val numCads: Int) {
    EASY(8),
    MEDIUM(18),
    HARD(24);

    companion object {
        fun getByValue(value: Int) = values().first { it.numCads == value }
    }

    fun getWidth(): Int {
        return when (this) {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight(): Int = numCads / getWidth()

    fun getPairs(): Int = numCads / 2
}