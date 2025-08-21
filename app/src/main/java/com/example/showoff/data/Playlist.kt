package com.example.showoff.data

data class Playlist(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val itemIds: List<String> = emptyList(),
    val coverUrl: String = "",
    val type: String = "",
    val dateCreated: String = ""


)
