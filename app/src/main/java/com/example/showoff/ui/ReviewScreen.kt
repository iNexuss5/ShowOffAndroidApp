    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.ArrowBack
    import androidx.compose.material.icons.filled.LockPerson
    import androidx.compose.material.icons.filled.People
    import androidx.compose.material.icons.filled.PeopleAlt
    import androidx.compose.material.icons.filled.PeopleOutline
    import androidx.compose.material.icons.filled.Person
    import androidx.compose.material.icons.filled.Star
    import androidx.compose.material.icons.filled.StarBorder
    import androidx.compose.material.icons.filled.StarHalf
    import androidx.compose.material.icons.filled.Visibility
    import androidx.compose.material.icons.filled.VisibilityOff
    import androidx.compose.material.icons.outlined.LockOpen
    import androidx.compose.material.icons.outlined.LockPerson
    import androidx.compose.material.icons.outlined.Person
    import androidx.compose.material.icons.outlined.Visibility
    import androidx.compose.material3.*
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import coil.compose.AsyncImage
    import com.example.showoff.viewmodel.HomeViewModel


    @Composable
    fun ReviewScreenUI(
        episodeId: String,
        viewModel: HomeViewModel,
        onBack: () -> Unit,
        onReviewSubmitted: () -> Unit
    ) {

        val context = LocalContext.current

        val rating = viewModel.rating.collectAsState().value
        val reviewText = viewModel.text.collectAsState().value

        val containsSpoiler by viewModel.containsSpoiler.collectAsState()
        val visibility by viewModel.visibility.collectAsState()


        val episodes by viewModel.allEpisodes.collectAsState()
        val episode = episodes.find { it.id == episodeId }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Review", color = Color.White, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cover and Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = episode?.coverUrl,
                        contentDescription = episode?.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${episode?.showName} - S${episode?.season} E${episode?.episode_number}",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        episode?.let {
                            Text(it.title, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rating stars
                Text("Your Rating", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    StarRating(rating = rating, onRatingChanged = { viewModel.setRating(it) })
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rating.toString(),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Your Review", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { viewModel.setText(it) },
                    placeholder = { Text("Write your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB084FF),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))


                Row(
                    horizontalArrangement = Arrangement.spacedBy(130.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpoilerIcon (
                        selected = containsSpoiler ,
                        onClick = { viewModel.toggleSpoiler() }
                    )
                    IsPrivateIcon (
                        selected = visibility,
                        onClick = { viewModel.toggleVisibility() }
                    )
                }


                Spacer(modifier = Modifier.height(20.dp))

                // Post Button
                Button(
                    onClick = {
                        episode?.let { viewModel.submitReview(it, onSuccess = onReviewSubmitted, context = context) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB084FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Post", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }

    @Composable
    fun StarRating(rating: Double, onRatingChanged: (Double) -> Unit) {
        val fullStars = rating.toInt()
        val hasHalfStar = rating - fullStars >= 0.5

        Row {
            for (i in 1..5) {
                val icon = when {
                    i <= fullStars -> Icons.Default.Star
                    i == fullStars + 1 && hasHalfStar -> Icons.Default.StarHalf
                    else -> Icons.Default.StarBorder
                }

                IconButton(onClick = {
                    val newRating = when {
                        rating == i.toDouble() -> i - 0.5
                        rating == i - 0.5 -> i.toDouble()
                        else -> i.toDouble()
                    }
                    onRatingChanged(newRating)
                }) {
                    Icon(
                        icon,
                        contentDescription = "Star $i",
                        tint = Color(0xFFB084FF),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun SpoilerIcon(
        selected: Boolean,
        onClick: () -> Unit
    ) {
        val iconColor = if (selected) Color(0xFFB084FF) else Color.Gray
        val textColor = if (selected) Color.White else Color.Gray
        val textContent = if (selected)  "Contains Spoiler" else "Spoiler-Free"
        val iconImage  = if (selected) Icons.Default.VisibilityOff else Icons.Default.Visibility

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = iconImage,
                contentDescription = textContent,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = textContent,
                    color = textColor,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

        }
    }

    @Composable
    fun IsPrivateIcon(
        selected: Boolean,
        onClick: () -> Unit
    ) {
        val iconColor = if (selected) Color(0xFFB084FF) else Color.Gray
        val textColor = if (selected) Color.White else Color.Gray
        val textContent = if (selected)   "Friends Only" else "Public"
        val iconImage  = if (selected)  Icons.Default.LockPerson else Icons.Outlined.LockOpen

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = iconImage,
                contentDescription = textContent,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = textContent,
                    color = textColor,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

        }
    }

