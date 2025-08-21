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
import com.example.showoff.viewmodel.CreateAccountViewModel

private lateinit var createAccountViewModel: CreateAccountViewModel

@Composable
fun CreateAccountUI(navController: NavController) {
    val context = LocalContext.current // To show Toast
    createAccountViewModel = viewModel()
    var username by remember { mutableStateOf(TextFieldValue()) }

    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        createAccountViewModel.createAccountEvents.collect { result ->
            val message = if (result.success) {
                "You are now logged in"
            } else {
                "Error: ${result.error ?: "Desconhecido"}"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if(result.success){
                navController.navigate("home") {
                    popUpTo("signup") { inclusive = true } // remove login da stack
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
            Spacer(modifier = Modifier.height(150.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
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

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    createAccountViewModel.createAccount(email.text,username.text, password.text, confirmPassword.text)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Criar Conta", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Sign in", color = Color.White)
                }
            }
        }
    }
}
