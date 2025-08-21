package com.example.showoff.util




import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController

import com.example.showoff.data.Episode
import com.example.showoff.data.Review
import com.example.showoff.data.Show
import com.example.showoff.data.TMDB.TMDbApi
import com.example.showoff.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.UUID

fun uploadEpisodes(episodes: List<Episode>) {
    val firestore = FirebaseFirestore.getInstance()


    for (ep in episodes) {
        firestore.collection("episodes")
            .document(ep.id) // usar ID personalizado
            .set(ep, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreUpload", "Show '${ep.title}' enviado com sucesso!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpload", "Erro ao enviar ${ep.title}: ${e.message}")
                e.printStackTrace()  // Isso vai fornecer mais detalhes sobre o erro
            }

    }
}



fun uploadUser(user: User) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(user.id) // Usa o ID fornecido (ex: UID do FirebaseAuth)
        .set(user)
        .addOnSuccessListener {
            Log.d("FirestoreUpload", "User '${user.username}' enviado com sucesso!")
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreUpload", "User '${user.username}' não foi enviado")
        }
}


fun fetchReviewCountForUser(userId: String, onResult: (Int) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("reviews")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { result ->
            onResult(result.size()) // número de documentos encontrados
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Erro ao buscar reviews do usuário: ${e.message}")
            onResult(0) // retorna 0 em caso de falha
        }
}


fun uploadReview(review: Review, context: Context, onSuccess: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    val reviewId = review.id.ifEmpty { UUID.randomUUID().toString() }

    db.collection("reviews")
        .document(reviewId)
        .set(review.copy(id = reviewId))
        .addOnSuccessListener {
            Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
            review.episodeId?.let { episodeId ->
                updateEpisodeAvgRating(db, episodeId)
            }
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to submit review: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun uploadProfileImage(uri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
    val uid = Firebase.auth.currentUser?.uid ?: return
    val imagePath = "avatars/$uid.jpg"
    val storageRef = Firebase.storage.reference.child(imagePath)

    storageRef.putFile(uri)
        .addOnSuccessListener {
            // Em vez de pegar o downloadUrl, armazenamos apenas o path no Firestore
            Firebase.firestore.collection("users").document(uid)
                .update("avatarUrl", imagePath)
                .addOnSuccessListener {
                    onSuccess(imagePath)  // Retorna o path, não a URL
                }
                .addOnFailureListener { e -> onError(e) }
        }
        .addOnFailureListener { e -> onError(e) }
}

fun updateUser(userId: String, newUsername: String, newAvatarUrl: String?, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val updates = mutableMapOf<String, Any>(
        "username" to newUsername
    )

    newAvatarUrl?.let {
        updates["avatarUrl"] = it
    }

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .update(updates)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e) }
}


fun updateEpisodeAvgRating(db: FirebaseFirestore, episodeId: String) {
    db.collection("reviews")
        .whereEqualTo("episodeId", episodeId)
        .get()
        .addOnSuccessListener { querySnapshot ->
            val reviews = querySnapshot.documents.mapNotNull { it.toObject(Review::class.java) }
            val totalRating = reviews.sumOf { it.rating }
            val avgRating = if (reviews.isNotEmpty()) totalRating.toDouble() / reviews.size else 0.0
            val roundedAvgRating = String.format(Locale.US, "%.1f", avgRating).toDouble()
            // Atualiza o episódio
            db.collection("episodes")
                .document(episodeId)
                .update("avgRating", roundedAvgRating)
        }
}


fun logout(navController: NavController) {
    FirebaseAuth.getInstance().signOut()
    UserManager.setUser(null)
    navController.navigate("login") {
        popUpTo(0) { inclusive = true } // Limpa o backstack
    }
}


fun resetAllRatings() {
    val db = FirebaseFirestore.getInstance()

    // Resetar episódios
    db.collection("episodes")
        .get()
        .addOnSuccessListener { result ->
            for (doc in result.documents) {
                doc.reference.update("avgRating", 0.0)
                    .addOnSuccessListener {
                        Log.d("Reset", "avgRating do episódio ${doc.id} resetado.")
                    }
                    .addOnFailureListener {
                        Log.e("Reset", "Erro ao resetar episódio ${doc.id}: ${it.message}")
                    }
            }
        }
        .addOnFailureListener {
            Log.e("Reset", "Erro ao buscar episódios: ${it.message}")
        }

    // Resetar séries
    db.collection("shows")
        .get()
        .addOnSuccessListener { result ->
            for (doc in result.documents) {
                doc.reference.update("avgRating", 0.0)
                    .addOnSuccessListener {
                        Log.d("Reset", "avgRating da série ${doc.id} resetado.")
                    }
                    .addOnFailureListener {
                        Log.e("Reset", "Erro ao resetar série ${doc.id}: ${it.message}")
                    }
            }
        }
        .addOnFailureListener {
            Log.e("Reset", "Erro ao buscar séries: ${it.message}")
        }
}

fun uploadShows(shows: List<Show>) {
    val firestore = FirebaseFirestore.getInstance()


    for (ep in shows) {
        firestore.collection("shows")
            .document(ep.id) // usar ID personalizado
            .set(ep, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreUpload", "Show '${ep.title}' enviado com sucesso!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpload", "Erro ao enviar ${ep.title}: ${e.message}")
                e.printStackTrace()  // Isso vai fornecer mais detalhes sobre o erro
            }

    }
}

fun fetchUserById(userId: String, onResult: (User?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                onResult(user)
            } else {
                Log.e("fetchUserById", "User not found for ID: $userId")
                onResult(null)
            }
        }
        .addOnFailureListener { e ->
            Log.e("fetchUserById", "Failed to fetch user: ${e.message}")
            onResult(null)
        }
}


fun fetchEpisodesFromFirestore(onResult: (List<Episode>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("episodes")
        .get()
        .addOnSuccessListener { result ->
            val episodeList = result.documents.mapNotNull { doc ->
                doc.toObject(Episode::class.java)
            }
            onResult(episodeList)
        }
        .addOnFailureListener {
            it.printStackTrace()
            onResult(emptyList())
        }
}


fun fetchShowsFromFirestore(onResult: (List<Show>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("shows")
        .get()
        .addOnSuccessListener { result ->
            val showList = result.documents.mapNotNull { doc ->
                doc.toObject(Show::class.java)
            }

            onResult(showList)
        }
        .addOnFailureListener {
            it.printStackTrace()
            onResult(emptyList())
        }

}


suspend fun fetchEpisodesFromTMDb(api: TMDbApi, apiKey: String): List<Episode> = withContext(
    Dispatchers.IO) {
    val allEpisodes = mutableListOf<Episode>()

    try {
        val popularShows = api.getPopularTV(apiKey).results

        for (show in popularShows) {
            val seasonResponse = api.getSeasonEpisodes(
                showId = show.id,
                season = 1,
                apiKey = apiKey
            )

            val episodes = seasonResponse.episodes.map { ep ->
                Episode(
                    id = ep.id.toString(),
                    showName = show.name,
                    title = ep.name,
                    description = ep.overview,
                    durationMinutes = ep.runtime,
                    avgRating = ep.vote_average,
                    coverUrl = ep.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" } ?: "https://image.tmdb.org/t/p/w500${show.poster_path}",
                    season = ep.season_number,
                    episode_number = ep.episode_number
                )
            }

            allEpisodes.addAll(episodes)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext allEpisodes

}
suspend fun fetchShowsFromTMDb(api: TMDbApi, apiKey: String): List<Show> = withContext(Dispatchers.IO) {
    val shows = mutableListOf<Show>()

    try {
        val popular = api.getPopularTV(apiKey).results

        for (show2 in popular) {
            val details = api.getShowDetails(show2.id, apiKey)

            val creatorName = details.created_by.firstOrNull()?.name ?: "Unknown"

            val show = Show(
                id = show2.id.toString(),
                title = show2.name,
                description = show2.overview,
                coverUrl = "https://image.tmdb.org/t/p/w500${show2.poster_path}",
                avgRating = show2.vote_average,
                creator = creatorName,
                air_date = show2.first_air_date
            )

            shows.add(show)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext shows
}

fun fetchEpisodesForShowFromFirestore(showName: String, onResult: (List<Episode>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("episodes")
        .whereEqualTo("showName", showName)   // Filtra só pelo show selecionado
        .get()
        .addOnSuccessListener { querySnapshot ->
            val episodes = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Episode::class.java)
            }
            onResult(episodes)
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Erro ao carregar episódios", exception)
            onResult(emptyList())
        }
}



fun uploadto(){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(TMDbApi::class.java)

    CoroutineScope(Dispatchers.IO).launch {
        //val episodes: List<Episode> = fetchEpisodesFromTMDb(api, "f17b7b9299b242680a0bf560c45d7073")
        val shows: List<Show> = fetchShowsFromTMDb(api,"f17b7b9299b242680a0bf560c45d7073" )
        // uploadEpisodes(episodes)
        uploadShows(shows)
    }

}

object UserManager {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun setUser(user: User?) {
        _currentUser.value = user
    }
}



fun getDownloadUrlFromStorage(
    path: String,
    onResult: (String?) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference.child(path)
    storageRef.downloadUrl
        .addOnSuccessListener { uri ->
            onResult(uri.toString())
        }
        .addOnFailureListener {
            onResult(null)
        }
}


object ReviewRepository {

    fun getAllReviews(onResult: (List<Review>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .get()
            .addOnSuccessListener { result ->
                val reviews = result.documents.mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.copy(id = doc.id)
                }
                onResult(reviews)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}



