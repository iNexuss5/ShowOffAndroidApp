package com.example.showoff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showoff.data.User
import com.example.showoff.util.UserManager
import com.example.showoff.util.uploadUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CreateAccountViewModel : ViewModel() {

    private val _createAccountEvents = MutableSharedFlow<LoginResult>() // Uso sharedflow porque ele emite sempre um
    // resultado, nao so quando é  mudado. dispara sempre que se faz .emit
    val createAccountEvents = _createAccountEvents.asSharedFlow()

    fun createAccount(email: String, username: String, password: String, confirmPassword: String) {
        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = formatter.format(date)

        viewModelScope.launch {
            if (email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                _createAccountEvents.emit(LoginResult(success = false, error = "All fields are required."))
                return@launch
            }
            if (password.length<6 ) {
                _createAccountEvents.emit(LoginResult(success = false, error = "Password must be at least 6 characters"))
                return@launch
            }

            if (password != confirmPassword ) {
                _createAccountEvents.emit(LoginResult(success = false, error = "Passwords do not match."))
                return@launch
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            _createAccountEvents.emit(LoginResult(success = true))

                            val firebaseUser = task.result?.user

                            // Se o Firebase retornar um usuário válido
                            firebaseUser?.let {
                                val newUser = User(
                                    id = it.uid,
                                    username = username,
                                    email = email,
                                    joinDate = formattedDate
                                )

                                uploadUser(newUser)
                                UserManager.setUser( newUser)

                            }
                        } else {
                            val errorMsg = task.exception?.message ?: "Something went wrong."
                            _createAccountEvents.emit(LoginResult(success = false, error = errorMsg))
                        }
                    }
                }
        }
    }

}


