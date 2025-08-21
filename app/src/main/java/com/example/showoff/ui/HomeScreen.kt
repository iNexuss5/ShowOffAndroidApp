package com.example.showoff.ui

import BottomNavigationBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.showoff.R
import com.example.showoff.data.Episode
import com.example.showoff.viewmodel.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.showoff.data.Show
import com.example.showoff.data.User
import com.example.showoff.util.UserManager
import com.example.showoff.util.fetchUserById
import com.example.showoff.util.getDownloadUrlFromStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun HomeScreenUI(navController: NavController, viewModel: HomeViewModel) {
    val episodes by viewModel.allEpisodes.collectAsState()
    val topEpisodes = episodes.sortedByDescending { it.avgRating ?: 0f }.take(4)

    var isAuthenticated by remember { mutableStateOf(false) }
    val user by UserManager.currentUser.collectAsState()
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            viewModel.searchShows(query)
            expanded = true
        } else {
            expanded = false
        }
    }

    LaunchedEffect(Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            isAuthenticated = true
            fetchUserById(firebaseUser.uid) { userFromDb ->
                val userToSet = userFromDb ?: User(
                    id = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )

                val avatarPath = userToSet.avatarUrl
                if (avatarPath != null) {
                    getDownloadUrlFromStorage(avatarPath) { downloadUrl ->
                        UserManager.setUser(userToSet.copy(avatarUrl = downloadUrl))
                    }
                } else {
                    UserManager.setUser(userToSet)
                }
            }
        } else {
            isAuthenticated = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .padding(bottom = 42.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    if (isAuthenticated) {
                        Text("Welcome Back,", color = Color.LightGray, fontSize = 16.sp)
                        user?.username?.let {
                            Text(it, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Browsing as guest", color = Color.LightGray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap here to sign in",
                            color = Color(0xFFB084FF),
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { navController.navigate("login") }
                        )
                    }
                }
                Box(modifier = Modifier.clickable { navController.navigate("profile") }) {
                    AvatarImage(path = user?.avatarUrl)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AutoComplete Search Field
            Box {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        expanded = it.isNotEmpty() && searchResults.isNotEmpty()
                    },
                    placeholder = { Text("Search your favourite show") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2C2F3F), shape = RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF2C2F3F),
                        focusedContainerColor = Color(0xFF2C2F3F),
                        cursorColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F212D))
                ) {
                    searchResults.forEach { show ->
                        DropdownMenuItem(
                            onClick = {
                                query = show.title
                                expanded = false
                                navController.navigate("show/${show.title}")
                            },
                            text = {
                                Text(
                                    text = show.title,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                AsyncImage(
                                    model = show.coverUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Coming Soon Section
            Text("Coming Soon", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Steel Ball Run",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Steel Ball Run", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("May 2025", color = Color.Gray)

            Spacer(modifier = Modifier.height(30.dp))

            // Best Rated Episodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Best Rated Episodes", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("See all", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                topEpisodes.forEach { episode ->
                    EpisodeItem(episode = episode) {
                        navController.navigate("details/${episode.id}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavigationBar(navController, 0)
        }
    }
}




@Composable
fun EpisodeItem(episode: Episode, onClick:() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()
        .clickable { onClick() } // clique no episódio
    ) {
        AsyncImage(
            model = episode.coverUrl,
            contentDescription = episode.title,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFFB084FF), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${episode.durationMinutes} min", fontSize = 12.sp, color = Color(0xFFB084FF))
            }
            Text(episode.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(episode.showName, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 0.dp)
            )

        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFB084FF), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(episode.avgRating?.toString() ?: "0.0", color = Color.White)
        }
    }
}

@Composable
fun AvatarImage(path: String?) {
    println(path)
    val size = 72.dp
    if (!path.isNullOrBlank()) {
        AsyncImage(
            model = path, // já é o download URL
            contentDescription = "Avatar",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Placeholder",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp)),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}


