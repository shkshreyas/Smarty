package com.shk.smarty.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Subject(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = ""
) {
    // Empty constructor needed for Firebase
    constructor() : this("", "", "", "")
} 