package utils

import android.graphics.drawable.Icon
import com.dimitrisligi.findtheanimals.R

object Constants {

    val DEFAULT_ICONS = listOf(
        R.drawable.ic_face,
        R.drawable.ic_flower,
        R.drawable.ic_gift,
        R.drawable.ic_heart,
        R.drawable.ic_home,
        R.drawable.ic_lighting,
        R.drawable.ic_moon,
        R.drawable.ic_plane,
        R.drawable.ic_school,
        R.drawable.ic_speaker,
        R.drawable.ic_star,
        R.drawable.ic_work,
    )

    const val TAG = "TAG"

    const val EXTRA_GAME_NAME = "EXTRA_GAME_NAME"

    const val CREATE_REQUEST_CODE = 245

    const val PICK_PHOTO_CODE = 183

    const val EXTRA_BOARD_SIZE = "EXTRA_BOARD_SIZE"

    const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE

    const val READ_EXTERNAL_PHOTOS_CODE = 224

    const val MIN_GAME_NAME_LENGTH = 3

    const val MAX_GAME_NAME_LENGTH = 14

    const val SCALING_HEIGHT = 250

    const val SCALING_QUALITY = 60
}