package models

import android.content.Context
import android.widget.Toast
import gameinterfaces.NotifyGameStatusInter
import utils.Constants

class GameManager (private val boardSize: BoardSize){
    val cards: List<MemoryCard>
    var numPairsFound: Int = 0

    private var indexOfSelectedCard: Int? = null
    private var numMoves: Int = 0

    init {
        /**
         * We want to take only a specific amount of card pairs according to the total amount board size.
         * So we create a shuffled list of the default icons,
         * and we take first icons equal to the  pair size.
         * e.g. If we have 12 cards to display, then we will take 6 pairs.
         */
        val chosenImages = Constants.DEFAULT_ICONS.shuffled().take(boardSize.getPairs())

        //We make a list that we are doubling the amount of the chosen images and the we shuffled them.
        val randomizedImages = (chosenImages + chosenImages).shuffled()

        //Mapping the images with the memory cards.
        cards = randomizedImages.map { MemoryCard(it) }

    }



    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier){
            return false
        }else{
            cards[position1].isMatched = true
            cards[position2].isMatched = true
            numPairsFound++
            return true
        }
    }

    fun flipCard(position: Int): Boolean{
        numMoves++
        val card =  cards[position]
        var foundMatch = false

        /**
         * We have 3 cased when we flip the card
         *
         * 0 cards previously flipped ->  restore all cards + flip over the card
         * 1 card previously flipped  ->  flip over the card and check if there is a match
         * 2 cars previously flipped  ->  restore all cards and flip this one as well
         */
        //First and third case
        if (indexOfSelectedCard == null){
            //0 cards or 2 cards are flipped over
            restoreCards()
            indexOfSelectedCard = position
        }else{
            foundMatch = checkForMatch(indexOfSelectedCard!!,position)
            indexOfSelectedCard = null
        }
        card.faceUp = !card.faceUp

        return foundMatch
    }

    private fun restoreCards() {
        cards.forEach {
            //If the card isn't matched then we flip it
            if (!it.isMatched) it.faceUp = false
        }
    }

    fun haveWonTheGame(): Boolean = numPairsFound == boardSize.getPairs()

    fun isCardFaceUp(position: Int): Boolean{
        return cards[position].faceUp
    }

    fun getTotalMoves():Int = numMoves / 2
}