package com.example.showoff.data

import java.util.Date

data class Review(
    val id: String = "",
    val userId: String = "",
    val showId: String? = null,
    val episodeId: String? = null, // nunca ambos preenchidos ao mesmo tempo
    val rating: Double = 0.0, // de 0 a 5
    val comment: String = "",
    val containsSpoiler: Boolean = false,
    val isPrivate: Boolean = false ,
    val datePosted: String = "",
    val coverURL: String = "",
    val title: String = ""
)