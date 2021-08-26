package com.dimitrisligi.findtheanimals

import adapters.PicPickerAdapter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import models.BoardSize
import utils.Constants

class CreateCustomGameActivity : AppCompatActivity() {

    private lateinit var boardSize: BoardSize
    private lateinit var rvPicPicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button

    private val chosenImagesURIs = mutableListOf<Uri>()
    private var numImagesRequired = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_custom_game)

        rvPicPicker = findViewById(R.id.rv_image_picker)
        etGameName = findViewById(R.id.et_game_name)
        btnSave = findViewById(R.id.btn_save_images)

        //Getting the data from the previous activity
        boardSize = intent.getSerializableExtra(Constants.EXTRA_BOARD_SIZE) as BoardSize

        //Adding back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Setting the number of images to pick
        numImagesRequired = boardSize.getPairs()

        //Setting the action bar
        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"

        rvPicPicker.adapter = PicPickerAdapter(this,chosenImagesURIs,boardSize)
        rvPicPicker.setHasFixedSize(true)
        rvPicPicker.layoutManager = GridLayoutManager(this,boardSize.getWidth())


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}