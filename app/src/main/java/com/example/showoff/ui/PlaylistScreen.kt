package com.example.showoff.ui

import BottomNavigationBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.showoff.data.Playlist
import com.example.showoff.util.UserManager
import com.example.showoff.viewmodel.HomeViewModel
import com.example.showoff.viewmodel.PlaylistViewModel

@Composable
fun PlaylistsScreenUI(
    navController: NavController,
    viewModel: PlaylistViewModel,
    homeViewModel: HomeViewModel = viewModel()
) {
    val playlists by viewModel.userPlaylists.collectAsState()
    val user by UserManager.currentUser.collectAsState()

    LaunchedEffect(user?.id) {
        user?.id?.let { viewModel.loadUserPlaylists(it) }
    }

    var showTypeDialog by remember { mutableStateOf(false) }
    var showSelectorDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedShowId by remember { mutableStateOf<String?>(null) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    var playlistTitle by remember { mutableStateOf("") }

    val shows by homeViewModel.searchResults.collectAsState()
    val episodes by homeViewModel.filteredEpisodes.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            Text(
                "Your Playlists",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (playlists.isEmpty()) {
                Text("No playlists found", color = Color.Gray)
            } else {
                playlists.forEach { playlist ->
                    PlaylistCard(playlist = playlist) {
                        navController.navigate("playlist/${playlist.id}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { showTypeDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 92.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Playlist")
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavigationBar(navController, 2)
        }

        if (showTypeDialog) {
            AlertDialog(
                onDismissRequest = { showTypeDialog = false },
                title = { Text("Select Playlist Type") },
                text = {
                    Column {
                        Button(onClick = {
                            selectedType = "shows"
                            showTypeDialog = false
                            showSelectorDialog = true
                            homeViewModel.searchShows("")
                        }) {
                            Text("Show")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            selectedType = "episodes"
                            showTypeDialog = false
                            showSelectorDialog = true
                            homeViewModel.searchShows("")
                        }) {
                            Text("Episode")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        if (showSelectorDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSelectorDialog = false
                    selectedShowId = null
                    selectedType = null
                    playlistTitle = ""
                    selectedItems = emptySet()
                },
                title = {
                    Text(
                        when (selectedType) {
                            "shows" -> "Select Shows"
                            "episodes" -> if (selectedShowId == null) "Select a Show" else "Select Episodes"
                            else -> ""
                        }
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = playlistTitle,
                            onValueChange = { playlistTitle = it },
                            label = { Text("Playlist name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )

                        when (selectedType) {
                            "shows" -> {
                                LazyColumn(modifier = Modifier.height(200.dp)) {
                                    items(shows) { show ->
                                        val isSelected = selectedItems.contains(show.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedItems = if (isSelected)
                                                        selectedItems - show.id
                                                    else
                                                        selectedItems + show.id
                                                }
                                                .padding(8.dp)
                                                .background(if (isSelected) Color(0xFF44475A) else Color.Transparent)
                                        ) {
                                            Text(show.title, color = Color.White)
                                        }
                                    }
                                }
                            }

                            "episodes" -> {
                                if (selectedShowId == null) {
                                    LazyColumn(modifier = Modifier.height(200.dp)) {
                                        items(shows) { show ->
                                            Text(
                                                text = show.title,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedShowId = show.id
                                                        homeViewModel.loadEpisodesForShow(show.id)
                                                    }
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.height(200.dp)) {
                                        items(episodes) { ep ->
                                            val isSelected = selectedItems.contains(ep.id)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedItems = if (isSelected)
                                                            selectedItems - ep.id
                                                        else
                                                            selectedItems + ep.id
                                                    }
                                                    .padding(8.dp)
                                                    .background(if (isSelected) Color(0xFF44475A) else Color.Transparent)
                                            ) {
                                                Text(ep.title, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cover = when (selectedType) {
                                "shows" -> shows.find { it.id == selectedItems.firstOrNull() }?.coverUrl ?: ""
                                "episodes" -> episodes.find { it.id == selectedItems.firstOrNull() }?.coverUrl ?: ""
                                else -> ""
                            }

                            if (playlistTitle.isNotBlank() && selectedItems.isNotEmpty()) {
                                viewModel.createPlaylist(
                                    title = playlistTitle,
                                    type = selectedType ?: "shows",
                                    itemIds = selectedItems.toList(),
                                    coverUrl = cover
                                )
                                showSelectorDialog = false
                                selectedItems = emptySet()
                                playlistTitle = ""
                                selectedShowId = null
                                selectedType = null
                            }
                        },
                        enabled = selectedItems.isNotEmpty() && playlistTitle.isNotBlank()
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSelectorDialog = false
                        selectedItems = emptySet()
                        playlistTitle = ""
                        selectedType = null
                        selectedShowId = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2F3F)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = playlist.coverUrl,
                contentDescription = playlist.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(playlist.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${playlist.itemIds.size} items", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
