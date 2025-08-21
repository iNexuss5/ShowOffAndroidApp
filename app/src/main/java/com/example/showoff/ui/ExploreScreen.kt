package com.example.showoff.ui

import BottomNavigationBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.showoff.R
import com.example.showoff.data.Review
import com.example.showoff.data.User
import com.example.showoff.util.UserManager
import com.example.showoff.util.ReviewRepository
import com.example.showoff.util.fetchUserById
import com.example.showoff.util.getDownloadUrlFromStorage
import com.example.showoff.viewmodel.ExploreViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExploreScreenUI(navController: NavController, viewModel: ExploreViewModel = viewModel()) {
    val reviews by viewModel.reviews.collectAsState()
    val user by UserManager.currentUser.collectAsState()

    var selectedTab by remember { mutableStateOf("Recent") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .padding(bottom = 42.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reviews", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.clickable { navController.navigate("profile") }) {

                AvatarImage(path = user?.avatarUrl)}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2C2F3F), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Recent", "Friends").forEach { label ->
                    val isSelected = selectedTab == label
                    Text(
                        text = label,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedTab = label }
                            .background(if (isSelected) Color(0xFFB084FF) else Color.Transparent)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reviews list
            if (selectedTab == "Recent") {
                reviews.forEach { review ->
                    ReviewItem(review = review, navController = navController)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                Text("Friends feed coming soon...", color = Color.Gray)
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {

            BottomNavigationBar(navController,1)
        }
    }
}
@Composable
fun ReviewItem(review: Review, navController: NavController) {
    var author by remember { mutableStateOf<User?>(null) }
    var authorPhotoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(review.userId) {
        fetchUserById(review.userId) { user ->
            author = user
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("details/${review.episodeId}") },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = review.coverURL,
                contentDescription = review.title,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(review.title, color = Color.White, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFB084FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(review.rating.toString(), color = Color.Gray, fontSize = 12.sp)
                }
                if (review.comment.isBlank()) {
                    Text(
                        text = "Speechless",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    val commentPreview = if (review.comment.length > 50) {
                        review.comment.take(50) + "..."
                    } else {
                        review.comment
                    }
                    Text("-$commentPreview", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        // Coluna do autor (foto + nome)
        author?.let { user ->
            LaunchedEffect(review.coverURL) {
                user.avatarUrl?.let {
                    getDownloadUrlFromStorage(it) { url ->
                        authorPhotoUrl = url
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 12.dp)
                    .clickable {
                        // Navega para perfil do autor da review, passando o userId
                        navController.navigate("profile/${user.id}")
                    }
            ) {
                AsyncImage(
                    model = authorPhotoUrl,
                    contentDescription = user.username,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.username,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}


