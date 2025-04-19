package com.shk.smarty.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Question(
    val id: String = "",
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val explanation: String = "",
    val quizId: String = ""
) {
    // Empty constructor needed for Firebase
    constructor() : this("", "", emptyList(), 0, "", "")
} 