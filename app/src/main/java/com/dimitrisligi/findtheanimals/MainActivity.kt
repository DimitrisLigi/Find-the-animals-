package com.dimitrisligi.findtheanimals

import adapters.MemoryBoardAdapter
import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Interpolator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gameinterfaces.ClickCardListener
import models.BoardSize
import models.GameManager
import utils.Constants

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
            //Refresh button
            R.id.mi_refresh ->{
                if(gameManager.getTotalMoves() > 0 && !gameManager.haveWonTheGame()) {
                    showAlertDialog("Do you want to restart?", null) { initRecycler() }
                }else return false
            }
            //Change board size button
            R.id.mi_change_board_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_create_custom_game -> {
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        //We let user first know what kind of difficulty first wants to create a custom game.
        //Creating the dialog board size view
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupButton = boardSizeView.findViewById<RadioGroup>(R.id.rg_change_difficulty)

        showAlertDialog("Choose difficulty for your custom game",boardSizeView) {
            val desiredBoardSize = when (radioGroupButton.checkedRadioButtonId) {
                R.id.rb_easy -> BoardSize.EASY
                R.id.rb_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
           val intent = Intent(this,CreateCustomGameActivity::class.java)
            intent.putExtra(Constants.EXTRA_BOARD_SIZE,desiredBoardSize)
            startActivityForResult(intent,Constants.CREATE_REQUEST_CODE)
        }
    }

    private fun showNewSizeDialog() {
        //Creating the dialog board size view
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupButton = boardSizeView.findViewById<RadioGroup>(R.id.rg_change_difficulty)

        when(boardSize){
            BoardSize.EASY -> radioGroupButton.check(R.id.rb_easy)
            BoardSize.MEDIUM -> radioGroupButton.check(R.id.rb_medium)
            BoardSize.HARD -> radioGroupButton.check(R.id.rb_hard)
        }

        showAlertDialog("Choose difficulty",boardSizeView) {
            boardSize = when (radioGroupButton.checkedRadioButtonId) {
                R.id.rb_easy -> BoardSize.EASY
                R.id.rb_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            initRecycler()
        }
    }

    private fun showAlertDialog(title:String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
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