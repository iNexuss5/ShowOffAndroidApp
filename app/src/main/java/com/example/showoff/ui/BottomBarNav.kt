import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController, selectedIndex:Int) {

    val selectedColor = Color(0xFFB084FF)
    val defaultColor = Color.Gray

    val items = listOf(
        Icons.Default.Home,
        Icons.Filled.Explore,
        Icons.Outlined.Bookmark,
        Icons.Filled.Person
    )

    val screens = listOf(
        "home",
        "explore", // Suponha que essa rota exista, senão substitui por outra ou remove
        "playlists", // Suponha que essa rota exista também
        "profile"
    )
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .offset(y = (-4).dp)
                .background(Color(0xFF7F5EAB))
        )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(74.dp)
                    .background(MaterialTheme.colorScheme.background),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = CenterVertically
            ) {
                items.forEachIndexed { index, icon ->
                    IconButton(
                        onClick = {
                            navController.navigate(screens[index]) {
                                launchSingleTop = true
                                popUpTo("home") { inclusive = false } // Altere conforme necessário
                            }
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .offset(y = -(12.dp))
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (index == selectedIndex) selectedColor else defaultColor,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

        }
    }
}