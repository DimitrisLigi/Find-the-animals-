package com.dimitrisligi.findtheanimals

import adapters.MemoryBoardAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import models.BoardSize
import utils.Constants

class MainActivity : AppCompatActivity() {

    /**
     * Declaring as lateinit vars the views
     */
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumberOfMoves: TextView
    private lateinit var tvNumberOfPairs: TextView

    private var boardSize: BoardSize = BoardSize.HARD



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initRecycler()
    }


    private fun initViews(){
        //Initializing the views
        rvBoard = findViewById(R.id.rv_board)
        tvNumberOfMoves = findViewById(R.id.tv_show_moves)
        tvNumberOfPairs = findViewById(R.id.tv_show_pairs)
    }



    private fun initRecycler(){

        /**
         * We want to take only a specific amount of card pairs according to the total amount board size.
         * e.g. If we have 12 cards to display, then we will take 6 pairs.
         */
        val chosenImages = Constants.DEFAULT_ICONS.shuffled().take(boardSize.getPairs())

        //We make a list that we are doubling the amount of the chosen images and the we shuffled them.
        val randomizedImages = (chosenImages + chosenImages).shuffled()


        //initializing gameBoard adapter
        rvBoard.adapter = MemoryBoardAdapter(this,boardSize, randomizedImages)

        //making rvBoard FIXED SIZE to improve our performance
        rvBoard.setHasFixedSize(true)

        //making the recyclerView grid
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }
}