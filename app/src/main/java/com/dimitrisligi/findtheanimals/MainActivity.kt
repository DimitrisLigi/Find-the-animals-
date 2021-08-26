package com.dimitrisligi.findtheanimals

import adapters.MemoryBoardAdapter
import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Interpolator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
    private var boardSize: BoardSize = BoardSize.HARD



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initializing the views
        initViews()

        //Initializing the recycler
        initRecycler()

    }

    //Inflating the menu bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    //Logic on items menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh ->{
                if(gameManager.getTotalMoves() > 0 && !gameManager.haveWonTheGame()) {
                    showAlertDialog("Do you want to restart?", null) { initRecycler() }
                }else return false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(title:String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setIcon(R.drawable.ic_refresh)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){
                _,_-> positiveClickListener.onClick(null)
            }.show()
    }


    private fun initViews(){
        //Initializing the views
        rvBoard = findViewById(R.id.rv_board)
        tvNumberOfMoves = findViewById(R.id.tv_show_moves)
        tvNumberOfPairs = findViewById(R.id.tv_show_pairs)



    }

    private fun initRecycler(){

        //Setting the textViews
        setUpTheTextViews()


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

    private fun setUpTheTextViews() {
        when(boardSize){
            BoardSize.EASY -> {
                tvNumberOfMoves.text = "Easy: 4 x 2"
                tvNumberOfPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumberOfMoves.text = "Medium: 6 x 3"
                tvNumberOfPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumberOfMoves.text = "Hard: 6 x 4"
                tvNumberOfPairs.text = "Pairs: 0 / 12"
            }
        }
        tvNumberOfPairs.setTextColor(ContextCompat.getColor(this,R.color.color_min_progress))
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
        //Flipping the card
        if(gameManager.flipCard(position)){
            //Creating a interpolated color
            val color = ArgbEvaluator().evaluate(
                gameManager.numPairsFound.toFloat() / boardSize.getPairs(),
                ContextCompat.getColor(this,R.color.color_min_progress),
                ContextCompat.getColor(this,R.color.color_max_progress)) as Int

            //Setting the color based the progress
            tvNumberOfPairs.setTextColor(color)

            //Update the number of pairs in the UI
            tvNumberOfPairs.text = "Pairs: ${gameManager.numPairsFound} / ${boardSize.getPairs()}"

            //Notify the user if won the game
            if (gameManager.haveWonTheGame()){
                Toast.makeText(this,"You Won!!!",Toast.LENGTH_LONG).show()
            }
        }

        tvNumberOfMoves.text = "Moves: ${gameManager.getTotalMoves()}"
        mAdapter.notifyDataSetChanged()
    }



}