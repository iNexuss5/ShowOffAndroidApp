package com.example.showoff.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.showoff.R
import com.example.showoff.viewmodel.LoginResult
import com.example.showoff.viewmodel.LoginViewModel

private lateinit var loginViewModel: LoginViewModel

@Composable
fun LoginUI(navController: NavController) {
    val context = LocalContext.current // To show Toast

    loginViewModel = viewModel()
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var loginResult by remember { mutableStateOf(LoginResult(success = false)) }

    LaunchedEffect(Unit) {
        loginViewModel.loginEvents.collect { result ->
            val message = if (result.success) {
                "You are now logged in."

            } else {
                "Error: ${result.error ?: "Unkown"}"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if(result.success){
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true } // remove login da stack
                }

            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val backgroundImage: Painter = painterResource(id = R.drawable.login_bg)
        Image(
            painter = backgroundImage,
            contentDescription = "Background",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuário") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { loginViewModel.login(username.text, password.text) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Entrar", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { /* TODO: recuperar senha */ }) {
                    Text("Forgot password?", color = Color.White)
                }
                TextButton(onClick = {
                    navController.navigate("signup")
                }) {
                    Text("Sign up", color = Color.White)
                }
            }
        }
    }
}
