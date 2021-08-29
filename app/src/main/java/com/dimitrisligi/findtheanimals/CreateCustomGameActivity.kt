package com.dimitrisligi.findtheanimals

import adapters.PicPickerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Adapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import gameinterfaces.ChoosePhotoImageClickListener
import models.BoardSize
import utils.BitmapScaler
import utils.Constants
import utils.PermissionsUtils
import java.io.ByteArrayOutputStream
import kotlin.coroutines.Continuation

class CreateCustomGameActivity : AppCompatActivity() {

    private lateinit var boardSize: BoardSize
    private lateinit var rvPicPicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var chooseImageAdapter : PicPickerAdapter

    private val chosenImagesURIs = mutableListOf<Uri>()
    private var numImagesRequired = -1
    private val storage = Firebase.storage
    private val db = Firebase.firestore

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

        //Making the edit text to be 14 maximum length
        etGameName.filters = arrayOf(InputFilter.LengthFilter(Constants.MAX_GAME_NAME_LENGTH))

        //Adding a listener on the edit text
        etGameName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO: EMPTY
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO: EMPTY
            }

            override fun afterTextChanged(s: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

        })

        btnSave.setOnClickListener {
            saveDataToFireBase()
        }

        //Calling the adapter
        adapterInit()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.READ_EXTERNAL_PHOTOS_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos()
            }else{
                Toast.makeText(this,"You can't create custom game if the application doesn't access your photos",Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != Constants.PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null){
            Log.w(Constants.TAG,"User probably cancel the action")
            return
        }else{
            val selectedUri = data.data
            val clipData = data.clipData
            if (clipData!= null){
                Log.i(Constants.TAG,"Clipdata numImages ${clipData.itemCount}: $clipData")
                for (i in 0 until clipData.itemCount){
                    val clipItem = clipData.getItemAt(i)
                    if (chosenImagesURIs.size < numImagesRequired){
                        chosenImagesURIs.add(clipItem.uri)
                    }
                }
            }else if(selectedUri != null){
                Log.i(Constants.TAG,"data: $selectedUri")
                chosenImagesURIs.add(selectedUri)
            }
            chooseImageAdapter.notifyDataSetChanged()

            supportActionBar?.title = "Choose pics ${chosenImagesURIs.size} / $numImagesRequired"

            btnSave.isEnabled = shouldEnableSaveButton()
        }
    }



    private fun shouldEnableSaveButton(): Boolean {
        if(chosenImagesURIs.size != numImagesRequired){
            return false
        }else if(etGameName.text.isBlank() || etGameName.text.length < Constants.MIN_GAME_NAME_LENGTH){
            return false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun adapterInit(){
        chooseImageAdapter = PicPickerAdapter(this,chosenImagesURIs,boardSize,object : ChoosePhotoImageClickListener{
            override fun onPlaceholderClicked() {
                val permissions = PermissionsUtils()
                if(permissions.isPermissionGranted(this@CreateCustomGameActivity,Constants.READ_PHOTOS_PERMISSION)){
                    launchIntentForPhotos()
                }else{
                    permissions.requestPermission(this@CreateCustomGameActivity,Constants.READ_PHOTOS_PERMISSION, Constants.READ_EXTERNAL_PHOTOS_CODE)
                }
            }

        })
        rvPicPicker.adapter = chooseImageAdapter
        rvPicPicker.setHasFixedSize(true)
        rvPicPicker.layoutManager = GridLayoutManager(this,boardSize.getWidth())
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        //We give the option for the user to choose multiple photos
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent,"Choose pics"),Constants.PICK_PHOTO_CODE)
    }

    private fun saveDataToFireBase() {
        val customGameName = etGameName.text.toString()
        var didWeEncounterWithError = false
        val uploadedImagesUrl = mutableListOf<String>()
        Log.i(Constants.TAG,"Saving data to firebase!")

        for((index,photoUri) in chosenImagesURIs.withIndex()){
            val imageByteArray = getImageArray(photoUri)
            /**Creating a model of the filepath. How our url should look like
             *eg.images/catGame/currentMills-index.gpg
             * */

            val filePath = "images/$customGameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoReferenceFilePath = storage.reference.child(filePath)
            //This line below is expensive
            photoReferenceFilePath.putBytes(imageByteArray).continueWithTask {
                photoUploadTask -> Log.i(Constants.TAG,"Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                photoReferenceFilePath.downloadUrl
            }.addOnCompleteListener {
                    downloadUrlTask -> if(!downloadUrlTask.isSuccessful){
                        Log.i(Constants.TAG,"Exeption with Firebase storage", downloadUrlTask.exception)
                        Toast.makeText(this,"Failed to upload images",Toast.LENGTH_LONG).show()
                        didWeEncounterWithError = true
                        return@addOnCompleteListener
                        }
                        if(didWeEncounterWithError){
                            return@addOnCompleteListener
                        }
                val downLoadUrl = downloadUrlTask.result.toString()
                uploadedImagesUrl.add(downLoadUrl)
                Log.i(Constants.TAG,"Finished uploading $photoUri, uploaded ${uploadedImagesUrl.size} photos")
                Toast.makeText(this,"Finished uploading the images",Toast.LENGTH_LONG).show()
                if (uploadedImagesUrl.size == chosenImagesURIs.size){
                    handleAllSuccessUploadedImages(customGameName,uploadedImagesUrl)
                }
            }
        }
    }

    private fun handleAllSuccessUploadedImages(gameName: String, imagesUrl: MutableList<String>) {
            //TODO: SOMETHING
    }

    /**
     * We don't want to upload big file of images. So we downgrading the images and making them
     * shorter files
     */
    private fun getImageArray(photoUri: Uri): ByteArray {

        val originalBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source = ImageDecoder.createSource(contentResolver,photoUri)
            ImageDecoder.decodeBitmap(source)
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
        }
        Log.i(Constants.TAG,"Original width: ${originalBitmap.width}, height: ${originalBitmap.height}")

        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap,250)

        Log.i(Constants.TAG,"After scaling width: ${scaledBitmap.width}, height: ${scaledBitmap.height}")

        val outPutStreamByteArray = ByteArrayOutputStream()

        //Quality 0 to 100..The bigger the better
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG,60,outPutStreamByteArray)

        return outPutStreamByteArray.toByteArray()

    }
}