package com.shk.smarty.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Module(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val subjectId: String = "",
    val quizzes: List<String> = emptyList() // List of quiz IDs
) {
    // Empty constructor needed for Firebase
    constructor() : this("", "", "", "", "", emptyList())
} 