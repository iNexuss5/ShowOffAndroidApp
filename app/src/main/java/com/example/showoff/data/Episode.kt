package com.example.showoff.data


data class Episode(
    val id: String = "",
    val showName: String = "",
    val title: String = "",
    val description: String = "",
    val durationMinutes: Int = 0,
    val avgRating : Float? = 0f,
    val season : Int = 1,
    val episode_number: Int = 0,
    val coverUrl: String = ""

)
