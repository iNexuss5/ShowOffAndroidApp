package com.example.showoff.data

import androidx.annotation.Keep

@Keep
data class Show(
    val id: String = "",
    val title: String = "",
    val creator: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val genres: List<String> = emptyList(),
    val avgRating : Float = 0f,
    val air_date: String =""
)
