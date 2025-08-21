package com.example.showoff.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showoff.data.Episode
import com.example.showoff.data.Review
import com.example.showoff.data.Show
import com.example.showoff.util.fetchEpisodesForShowFromFirestore
import com.example.showoff.util.fetchEpisodesFromFirestore
import com.example.showoff.util.fetchShowsFromFirestore
import com.example.showoff.util.uploadReview
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HomeViewModel : ViewModel() {
    private val _allEpisodes = MutableStateFlow<List<Episode>>(emptyList())
    val allEpisodes: StateFlow<List<Episode>> = _allEpisodes

    private val _filteredEpisodes = MutableStateFlow<List<Episode>>(emptyList())
    val filteredEpisodes: StateFlow<List<Episode>> = _filteredEpisodes

    private val _allShows = MutableStateFlow<List<Show>>(emptyList())
    val allShows :StateFlow<List<Show>> = _allShows

    private val _searchResults = MutableStateFlow<List<Show>>(emptyList())
    val searchResults: StateFlow<List<Show>> = _searchResults

    init {
        fetchEpisodesFromFirestore { fetched ->
            _allEpisodes.value = fetched
            _filteredEpisodes.value = emptyList()        }

        fetchShowsFromFirestore { fetched ->
            _allShows.value = fetched
            _searchResults.value = emptyList() // Mostrar todos inicialmente
        }

    }



    fun searchShows(query: String) {
        viewModelScope.launch {
            val shows = if (query.isBlank()) {
                _allShows.value
            } else {
                _allShows.value.filter {
                    it.title.startsWith(query, ignoreCase = true)
                }
            }

            _searchResults.value = shows.sortedBy { it.title }
        }
    }


    private val _rating = MutableStateFlow(0.0)
    val rating: StateFlow<Double> = _rating

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    fun setRating(value: Double) {
        _rating.value = value
    }

    fun setText(value: String) {
        _text.value = value
    }


    private val _containsSpoiler = MutableStateFlow(false)
    val containsSpoiler: StateFlow<Boolean> = _containsSpoiler

    private val _visibility = MutableStateFlow(false)
    val visibility: StateFlow<Boolean> = _visibility

    fun toggleSpoiler() {
        _containsSpoiler.value = !_containsSpoiler.value
    }

    fun toggleVisibility() {
     _visibility.value = !_visibility.value
    }

    // Aqui você pode chamar Firestore ou Room para salvar o review
    fun submitReview(episode: Episode, onSuccess: () -> Unit, context: Context) {
        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = formatter.format(date)


        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid



        val review = uid?.let {
            Review(
                id = UUID.randomUUID().toString(), // Gera um ID único para a review
                userId = it,          // Substitua com a ID do usuário atual
                episodeId = episode.id,      // Exemplo de episódio; use showId se for review de show
                rating = _rating.value,                      // Nota de 0 a 5
                comment = _text.value,
                containsSpoiler = _containsSpoiler.value,            // Ajuste conforme necessário
                isPrivate = _visibility.value,     // Ou outra visibilidade definida
                datePosted = formattedDate,
                title = episode.title,
                coverURL = episode.coverUrl
            )
        }
        if (review != null) {
            uploadReview(review, context, onSuccess)
            println(review)
        }

    }


    fun loadEpisodesForShow(showName: String) {
        viewModelScope.launch {
            fetchEpisodesForShowFromFirestore(showName) { episodes ->
                _filteredEpisodes.value = episodes.sortedBy { it.episode_number }
            }
        }
    }




}

