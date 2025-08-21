package com.example.showoff.data

import java.util.Date

data class User(
    val id: String = "",
    var username: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val isPremium: Boolean = false,
    val friends: List<String> = emptyList(),
    val playlists: List<String> = emptyList(),
    val joinDate: String = "",
)
