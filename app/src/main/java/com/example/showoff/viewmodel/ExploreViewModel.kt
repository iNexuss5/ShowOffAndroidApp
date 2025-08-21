package com.example.showoff.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showoff.data.Review
import com.example.showoff.util.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    init {
        loadRecentReviews()
    }

    private fun loadRecentReviews() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        viewModelScope.launch {
            ReviewRepository.getAllReviews { allReviews ->
                val filtered = allReviews
                    .filter { it.userId != currentUserId }
                    .sortedByDescending { it.datePosted }

                _reviews.value = filtered
            }
        }
    }
}
