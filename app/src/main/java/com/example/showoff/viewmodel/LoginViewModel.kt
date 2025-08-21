package com.example.showoff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showoff.data.User
import com.example.showoff.util.uploadUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.showoff.util.UserManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LoginResult(val success: Boolean, val error: String? = null)

class LoginViewModel : ViewModel() {
    private val _loginEvents = MutableSharedFlow<LoginResult>()
    val loginEvents = _loginEvents.asSharedFlow()

    fun login(email: String, password: String) {

        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = formatter.format(date)

        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _loginEvents.emit(LoginResult(success = false, error = "All fields are required."))
                return@launch
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            if (firebaseUser == null) {
                                _loginEvents.emit(LoginResult(success = false, error = "Erro: usuário não encontrado após login."))
                                return@launch
                            }

                            val db = FirebaseFirestore.getInstance()

                            // Busca no Firestore se já existe usuário com este email
                            db.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        // Não existe usuário - cria um novo
                                        val newUser = User(
                                            id = firebaseUser.uid,
                                            username = firebaseUser.displayName ?: "",
                                            email = email,
                                            joinDate = formattedDate
                                        )
                                        uploadUser(newUser)
                                        UserManager.setUser( newUser)

                                    } else {
                                        val user = documents.documents[0].toObject(User::class.java)
                                        if (user != null) {
                                            UserManager.setUser(user)
                                        }
                                    }


                                    viewModelScope.launch {
                                        _loginEvents.emit(LoginResult(success = true))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    viewModelScope.launch {
                                        _loginEvents.emit(LoginResult(success = false, error = "Erro ao buscar usuário no Firestore: ${e.message}"))
                                    }
                                }
                        } else {
                            _loginEvents.emit(LoginResult(success = false, error = task.exception?.message ?: "Erro desconhecido"))
                        }
                    }
                }
        }
    }
}

