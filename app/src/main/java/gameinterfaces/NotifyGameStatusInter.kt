package gameinterfaces

interface NotifyGameStatusInter {
    fun isGameWon(pairs:Boolean):Boolean
    fun notifyTheUser(): String
}