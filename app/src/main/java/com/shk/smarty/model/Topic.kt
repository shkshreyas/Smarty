package com.shk.smarty.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Topic(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subjectId: String = "",
    val imageUrl: String = ""
) {
    // Empty constructor needed for Firebase
    constructor() : this("", "", "", "", "")
} 