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
import com.example.showoff.data.Episode
import com.example.showoff.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowScreenUI(showName: String, navController: NavController, viewModel: HomeViewModel) {

    LaunchedEffect(showName) {
        viewModel.loadEpisodesForShow(showName)
    }

    val episodesState by viewModel.filteredEpisodes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(showName) }, // Placeholder
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            episodesState == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            episodesState!!.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No episodes available.")
                }
            }

            else -> {
                val groupedEpisodes = episodesState!!.groupBy { it.season }

                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    groupedEpisodes.forEach { (season, episodesInSeason) ->
                        item {
                            SeasonSection(season = season, episodes = episodesInSeason) { episode ->
                                navController.navigate("details/${episode.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeasonSection(
    season: Int,
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Season $season",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        if (expanded) {
            episodes.forEach { episode ->
                Card(
                    onClick = { onEpisodeClick(episode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ep ${episode.episode_number}: ${episode.title}")
                    }
                }
            }
        }
    }
}
