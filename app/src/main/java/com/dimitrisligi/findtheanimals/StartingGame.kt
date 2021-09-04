package com.dimitrisligi.findtheanimals

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import models.BoardSize
import utils.Constants

class StartingGame : AppCompatActivity() {
    private lateinit var btnStartGame: Button
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting_game)
        initViews()
        val intent = Intent(this,MainActivity::class.java)

        btnEasy.setOnClickListener {
            btnStartGame.isEnabled = true
            intent.putExtra(Constants.EXTRA_BOARD_SIZE,BoardSize.EASY)
        }
        btnMedium.setOnClickListener {
            btnStartGame.isEnabled = true
            intent.putExtra(Constants.EXTRA_BOARD_SIZE,BoardSize.MEDIUM)
        }
        btnHard.setOnClickListener {
            btnStartGame.isEnabled = true
            intent.putExtra(Constants.EXTRA_BOARD_SIZE,BoardSize.HARD)
        }
        btnStartGame.setOnClickListener {
            startActivity(intent)
        }

    }

    private fun initViews(){
        btnStartGame = findViewById(R.id.btn_start_game)
        btnEasy = findViewById(R.id.btn_easy)
        btnMedium = findViewById(R.id.btn_medium)
        btnHard = findViewById(R.id.btn_hard)
    }
}