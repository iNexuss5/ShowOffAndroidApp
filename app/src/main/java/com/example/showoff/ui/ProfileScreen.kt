package com.example.showoff.ui

import BottomNavigationBar
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.showoff.data.Playlist
import com.example.showoff.data.User
import com.example.showoff.util.UserManager
import com.example.showoff.util.fetchReviewCountForUser
import com.example.showoff.util.fetchUserById
import com.example.showoff.util.getDownloadUrlFromStorage
import com.example.showoff.util.logout
import com.example.showoff.viewmodel.PlaylistViewModel

@Composable
fun ProfileScreenUI(navController: NavController,userId : String?=null, viewModel: PlaylistViewModel = viewModel()) {
    val currentUser = UserManager.currentUser.collectAsState()
    val displayedUserId = userId ?: currentUser.value?.id


    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(displayedUserId) {
        if (displayedUserId != null) {
            fetchUserById(displayedUserId) { fetchedUser ->
                user = fetchedUser

            }
        }
    }


    val playlists by viewModel.userPlaylists.collectAsState()

    LaunchedEffect(user?.id) {
        user?.id?.let { viewModel.loadUserPlaylists(it) }
    }

    val reviewCount = remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Your Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IsPremiumAvatarImage(user?.avatarUrl, isisPremium = user?.isPremium == true)

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (user?.isPremium == true) {
                        Text(
                            "isPremium",
                            color = Color(0xFFB084FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        user?.username ?: "Guest",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Joined in ${user?.joinDate ?: "N/A"}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (currentUser.value?.username == user?.username) {
                        Button(
                            onClick = { navController.navigate("edit_profile") },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Edit Profile")
                        }
                    }
                }
                LaunchedEffect(user?.id) {
                    user?.id?.let {
                        fetchReviewCountForUser(it) { count ->
                            reviewCount.value = count
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    IconWithCount(Icons.Default.Edit, reviewCount.value)
                    Spacer(modifier = Modifier.height(12.dp))
                    IconWithCount(Icons.Default.Group, 0)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Playlists", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("See all", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (playlists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentVeryDissatisfied,
                        contentDescription = "No Playlists",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Uhoh! This user has no playlists!", color = Color.Gray)
                }
            } else {
                Column {
                    playlists.forEach { playlist ->
                        PlaylistCard(playlist = playlist, onClick = {
                            navController.navigate("playlist/${playlist.id}")
                        })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(
                    onClick = { logout(navController) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 20.dp, end = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = Color.Gray)
                }

                if (currentUser.value?.username == user?.username) {
                    BottomNavigationBar(navController, 3)

                }else{
                    BottomNavigationBar(navController, -1)

                }

            }
        }
    }
}

@Composable
fun IsPremiumAvatarImage(downloadUrl: String?, isisPremium: Boolean) {
    var resolvedUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(downloadUrl) {
        if (!downloadUrl.isNullOrBlank()) {
            getDownloadUrlFromStorage(downloadUrl) { url ->
                resolvedUrl = url
            }
        }
    }

    val borderModifier = if (isisPremium) Modifier
        .border(3.dp, Color(0xFFB084FF), RoundedCornerShape(12.dp))
    else Modifier

    Box(
        modifier = borderModifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (!downloadUrl.isNullOrBlank()) {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun IconWithCount(icon: ImageVector, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text(count.toString(), color = Color.White)
    }
}
