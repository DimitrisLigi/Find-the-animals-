package com.dimitrisligi.findtheanimals

import adapters.MemoryBoardAdapter
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gameinterfaces.ClickCardListener
import models.BoardSize
import models.GameManager

class MainActivity : AppCompatActivity() {



    /**
     * Declaring as lateinit vars the views
     */
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumberOfMoves: TextView
    private lateinit var tvNumberOfPairs: TextView


    //GAME MANAGER
    private lateinit var gameManager: GameManager

    //ADAPTER
    private lateinit var mAdapter: MemoryBoardAdapter

    //We create a board size equal to the level of hardness.
    private var boardSize: BoardSize = BoardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initializing the views
        initViews()

        //Initializing the recycler
        initRecycler()

    }

    private fun initViews(){
        //Initializing the views
        rvBoard = findViewById(R.id.rv_board)
        tvNumberOfMoves = findViewById(R.id.tv_show_moves)
        tvNumberOfPairs = findViewById(R.id.tv_show_pairs)
    }

    private fun initRecycler(){

        //Initializing gameManager
        gameManager = GameManager(boardSize)

        //initializing gameBoard adapter
        mAdapter = MemoryBoardAdapter(this, boardSize, gameManager.cards,object :
            ClickCardListener{
            override fun onCardClicked(position: Int) {
                updateCardAfterClick(position)
            }
        })

        rvBoard.adapter= mAdapter

        //making rvBoard FIXED SIZE to improve our performance
        rvBoard.setHasFixedSize(true)

        //making the recyclerView grid
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun updateCardAfterClick(position: Int) {
        //Case we have won the game
        if(gameManager.haveWonTheGame()){
            return
        }
        //Case we already have a face up card
        if (gameManager.isCardFaceUp(position)){
            return
        }
        //Else flip the card and notify adapter
        if(gameManager.flipCard(position)){
            tvNumberOfPairs.text = "Pairs: ${gameManager.numPairsFound} / ${boardSize.getPairs()}"
        }
        tvNumberOfMoves.text = "Moves: ${gameManager.getTotalMoves()}"
        mAdapter.notifyDataSetChanged()
    }
}