package com.shk.smarty.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Quiz(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val topicId: String = "",
    val subjectId: String = "",
    val timeLimit: Int = 10,
    val passingPercentage: Int = 70
) {
    // Empty constructor needed for Firebase
    constructor() : this("", "", "", "", "", 10, 70)
} 