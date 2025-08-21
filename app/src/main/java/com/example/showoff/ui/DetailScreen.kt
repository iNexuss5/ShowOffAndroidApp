import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.showoff.data.Episode
import com.example.showoff.viewmodel.HomeViewModel

@Composable
fun DetailsScreenUI(
    episodeId: String,
    viewModel: HomeViewModel,
    onReviewClick: () -> Unit,
    onBack: () -> Unit
) {
    val episodes by viewModel.allEpisodes.collectAsState()
    val episode = episodes.find { it.id == episodeId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp) // Altura padrão de app bar
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (episode == null) {
                    Text("Episódio não encontrado.", color = Color.Red)
                    return@Column
                }

                AsyncImage(
                    model = episode.coverUrl,
                    contentDescription = episode.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${episode.showName} - S${episode.season} E${episode.episode_number}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = episode.title,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${episode.avgRating}", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(text = "Action")
                    Chip(text = "Fiction Fantasy")
                    Chip(text = "${episode.durationMinutes}m")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Synopsis", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = episode.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = onReviewClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB084FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Review", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2C2F3F),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontSize = 12.sp)
        }
    }
}



