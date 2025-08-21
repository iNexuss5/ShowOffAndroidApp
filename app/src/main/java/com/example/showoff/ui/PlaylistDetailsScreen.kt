package com.example.showoff.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.example.showoff.viewmodel.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(playlistId: String, navController: NavController, viewModel: PlaylistViewModel) {
    val playlists by viewModel.userPlaylists.collectAsState()
    val playlist = playlists.find { it.id == playlistId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.title ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (playlist != null) {
                Text("Tipo: ${playlist.type}")
                Text("Criado em: ${playlist.dateCreated}")
                Spacer(modifier = Modifier.height(16.dp))

                Text("Conteúdo da playlist:")
                playlist.itemIds.forEach { showId ->
                    Text("• $showId") // Aqui deverias buscar os detalhes do show se quiseres mais info
                }
            } else {
                Text("Playlist não encontrada.")
            }
        }
    }
}
