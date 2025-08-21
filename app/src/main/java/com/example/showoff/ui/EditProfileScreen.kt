package com.example.showoff.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.showoff.util.UserManager
import com.example.showoff.util.updateUser
import com.example.showoff.util.uploadProfileImage
import java.net.URLEncoder

@Composable
fun EditProfileScreen(navController: NavController) {
    val user by UserManager.currentUser.collectAsState()
    var newUsername by remember { mutableStateOf("") }
    var profileImagePath by remember { mutableStateOf("") } // Track image path directly
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            uploadProfileImage(it,
                onSuccess = { path ->
                    profileImagePath = path // Update state with image path
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    LaunchedEffect(user) {
        user?.let {
            newUsername = it.username
            if (profileImagePath.isEmpty()) {
                profileImagePath = it.avatarUrl ?: ""
            }
        }
    }


    // Generate full image URL when profileImagePath is available
    val fullImageUrl = if (profileImagePath.isNotEmpty()) {
        val encodedPath = URLEncoder.encode(profileImagePath, "UTF-8")
        "https://firebasestorage.googleapis.com/v0/b/showoff-79a96.firebasestorage.app/o/$encodedPath?alt=media"
    } else {
        "" // Return an empty string if no image
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Picture
            if (fullImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = fullImageUrl, // Use the full image URL
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                println(fullImageUrl)
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add Picture Button
            TextButton(onClick = {
                launcher.launch("image/*")
            }) {
                Text("Add Picture", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Edit Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Username Input
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("Username") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(onClick = {
                user?.let { currentUser ->
                    updateUser(
                        userId = currentUser.id,
                        newUsername = newUsername,
                        newAvatarUrl = profileImagePath.takeIf { it.isNotEmpty() },
                        onSuccess = {
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            UserManager.setUser(currentUser.copy(username = newUsername, avatarUrl = profileImagePath))
                            navController.popBackStack()
                        },
                        onError = {
                            Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }) {
                Text("Save Changes")
            }
        }
    }
}
