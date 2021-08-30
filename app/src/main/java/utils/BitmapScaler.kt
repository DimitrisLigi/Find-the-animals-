package utils

import android.graphics.Bitmap

object BitmapScaler {



    /**
     * Scale and maintain aspect ratio given a desired width
     */
    fun scaleToFitWidth(bitmap: Bitmap, width: Int): Bitmap{
        val factor = width / bitmap.width.toFloat()
        return Bitmap.createScaledBitmap(bitmap,width,(bitmap.height * factor).toInt(),true)
    }

    /**
     * Scale and maintain aspect ratio give a desired height
     */
    fun scaleToFitHeight(bitmap: Bitmap, height: Int): Bitmap{
        val factor = height / bitmap.height.toFloat()
        return Bitmap.createScaledBitmap(bitmap,(bitmap.width * factor).toInt(),height,true)
    }
}