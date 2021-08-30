package models

import com.google.firebase.firestore.PropertyName

data class UserCustomGameImageList(
    @PropertyName("images")
    val images: List<String>? = null
)
