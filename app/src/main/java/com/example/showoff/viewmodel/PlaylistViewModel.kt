package com.example.showoff.viewmodel

import androidx.lifecycle.ViewModel
import com.example.showoff.data.Playlist
import com.example.showoff.util.UserManager
import com.google.firebase.Firebase

import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PlaylistViewModel : ViewModel() {

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists

    fun loadUserPlaylists(userId: String) {
        Firebase.firestore.collection("playlists")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val playlists = result.documents.mapNotNull { it.toObject(Playlist::class.java) }
                _userPlaylists.value = playlists
            }
    }

    fun createPlaylist(
        title: String,
        type: String,
        itemIds: List<String>,
        coverUrl: String = "",
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val userId = UserManager.currentUser.value?.id ?: return
        val newDocRef =   Firebase.firestore.collection("playlists").document()
        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = formatter.format(date)

        val playlist = Playlist(
            id = newDocRef.id,
            userId = userId,
            title = title,
            type = type,
            itemIds = itemIds,
            coverUrl = coverUrl,
            dateCreated = formattedDate
        )

        newDocRef.set(playlist)
            .addOnSuccessListener {
                loadUserPlaylists(userId) // opcional: recarrega listas
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


}
