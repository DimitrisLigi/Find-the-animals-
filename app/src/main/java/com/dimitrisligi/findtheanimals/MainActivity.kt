package com.dimitrisligi.findtheanimals

import adapters.MemoryBoardAdapter
import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import gameinterfaces.ClickCardListener
import models.BoardSize
import models.GameManager
import models.UserCustomGameImageList
import utils.Constants

class MainActivity : AppCompatActivity() {

    /**
     * Declaring as lateinit vars the views
     */
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumberOfMoves: TextView
    private lateinit var tvNumberOfPairs: TextView
    private lateinit var clRoot: ConstraintLayout
    private lateinit var boardSize: BoardSize
    //GAME MANAGER
    private lateinit var gameManager: GameManager

    //ADAPTER
    private lateinit var mAdapter: MemoryBoardAdapter

    //We create a board size equal to the level of hardness.
//    private var boardSize: BoardSize = BoardSize.EASY


    //Firestore instance
    private val db = Firebase.firestore

    //The custom game we receive
    private var gameName: String? = null

    //The custom game images
    private var customGameImages: List<String>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        boardSize = intent.getSerializableExtra(Constants.EXTRA_BOARD_SIZE) as BoardSize

        //Initializing the views
        initViews()

        //Initializing the recycler
        initRecycler()

    }

    //Inflating the menu bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //Logic on items menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Refresh button
            R.id.mi_refresh -> {
                if (gameManager.getTotalMoves() > 0) {
                    showAlertDialog("Do you want to restart?", null) {
                        initRecycler()
                    }
                } else return false
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
            R.id.mi_download_custom_game ->{
                showDownloadDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == Constants.CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customGameName = data?.getStringExtra(Constants.EXTRA_GAME_NAME)
            if (customGameName == null){
                Log.e(Constants.TAG,"Custom game return's null from createCustomGameActivity")
                return
            }
            downLoadCustomGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downLoadCustomGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { it ->
            val imageList = it.toObject(UserCustomGameImageList::class.java)
            if(imageList?.images == null){
                Log.e(Constants.TAG,"Invalid custom data from Firestore")
                Snackbar.make(clRoot,"Sorry, we didn't find any " +
                        "$customGameName game in our database",Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val numCards = imageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            customGameImages = imageList.images
            for(imageUrl in imageList.images){
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot,"You are playing $customGameName",Snackbar.LENGTH_LONG).show()
            this.gameName = customGameName
            initRecycler()
        }.addOnFailureListener {
            Log.e(Constants.TAG,"Exception error retrieving the game",it)
        }

    }


    private fun showCreationDialog() {
        //We let user first know what kind of difficulty first wants to create a custom game.
        //Creating the dialog board size view
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupButton = boardSizeView.findViewById<RadioGroup>(R.id.rg_change_difficulty)

        showAlertDialog("Choose difficulty for your custom game", boardSizeView) {
            val desiredBoardSize = when (radioGroupButton.checkedRadioButtonId) {
                R.id.rb_easy -> BoardSize.EASY
                R.id.rb_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            val intent = Intent(this, CreateCustomGameActivity::class.java)

            intent.putExtra(Constants.EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, Constants.CREATE_REQUEST_CODE)
        }
    }

    private fun showNewSizeDialog() {
        //Creating the dialog board size view
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupButton = boardSizeView.findViewById<RadioGroup>(R.id.rg_change_difficulty)

        when (boardSize) {
            BoardSize.EASY -> radioGroupButton.check(R.id.rb_easy)
            BoardSize.MEDIUM -> radioGroupButton.check(R.id.rb_medium)
            BoardSize.HARD -> radioGroupButton.check(R.id.rb_hard)
        }

        showAlertDialog("Choose difficulty", boardSizeView) {
            boardSize = when (radioGroupButton.checkedRadioButtonId) {
                R.id.rb_easy -> BoardSize.EASY
                R.id.rb_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            customGameImages = null
            initRecycler()
        }
    }


    private fun showDownloadDialog() {
        val boardDownloadCustomGameView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board,null)
        showAlertDialog("Download a custom game",boardDownloadCustomGameView,View.OnClickListener {
            val etDownloadCustomGame = boardDownloadCustomGameView.findViewById<EditText>(R.id.et_download_custom_game)
            val gameToDownLoad = etDownloadCustomGame.text.toString().trim()
        downLoadCustomGame(gameToDownLoad)
        })
    }


    private fun showAlertDialog(
        title: String,
        view: View?,
        positiveClickListener: View.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok") { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }


    private fun showWinningAlertDialog(
        title: String,
        view: View?,
        positiveClickListener: View.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel"){_,_ ->finish()}
            .setPositiveButton("Ok") { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }


    private fun initViews() {
        //Initializing the views
        rvBoard = findViewById(R.id.rv_board)
        tvNumberOfMoves = findViewById(R.id.tv_show_moves)
        tvNumberOfPairs = findViewById(R.id.tv_show_pairs)
        clRoot = findViewById(R.id.clRoot)

    }

    private fun initRecycler() {

        //Setting the textViews
        setUpTheTextViews()


        //Initializing gameManager
        gameManager = GameManager(boardSize, customGameImages)

        //initializing gameBoard adapter
        mAdapter = MemoryBoardAdapter(this, boardSize, gameManager.cards, object :
            ClickCardListener {
            override fun onCardClicked(position: Int) {
                updateCardAfterClick(position)
            }
        })

        rvBoard.adapter = mAdapter

        //making rvBoard FIXED SIZE to improve our performance
        rvBoard.setHasFixedSize(true)

        //making the recyclerView grid
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun setUpTheTextViews() {
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        when (boardSize) {
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
        tvNumberOfPairs.setTextColor(ContextCompat.getColor(this, R.color.color_min_progress))
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun updateCardAfterClick(position: Int) {
        //Case we have won the game
        if (gameManager.haveWonTheGame()) {
            return
        }
        //Case we already have a face up card
        if (gameManager.isCardFaceUp(position)) {
            return
        }
        //Flipping the card
        if (gameManager.flipCard(position)) {
            //Creating a interpolated color
            val color = ArgbEvaluator().evaluate(
                gameManager.numPairsFound.toFloat() / boardSize.getPairs(),
                ContextCompat.getColor(this, R.color.color_min_progress),
                ContextCompat.getColor(this, R.color.color_max_progress)
            ) as Int

            //Setting the color based the progress
            tvNumberOfPairs.setTextColor(color)

            //Update the number of pairs in the UI
            tvNumberOfPairs.text = "Pairs: ${gameManager.numPairsFound} / ${boardSize.getPairs()}"

            //Notify the user if won the game
            if (gameManager.haveWonTheGame()) {
                val v = CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.BLUE,Color.GREEN,Color.RED)).oneShot()
                showWinningAlertDialog("You have won! Do you want to restart?",null,View.OnClickListener{
                    v.terminate()
                    initRecycler()
                })

            }
        }
        tvNumberOfMoves.text = "Moves: ${gameManager.getTotalMoves()}"
        mAdapter.notifyDataSetChanged()
    }
}


