package com.example.emptyactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emptyactivity.ui.theme.EmptyActivityTheme

enum class AuthScreenState { LOGIN, REGISTER, HOME }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var screenState by remember { mutableStateOf(AuthScreenState.LOGIN) }
            var userName by remember { mutableStateOf("") }
            var authToken by remember { mutableStateOf<String?>(null) }

            EmptyActivityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (screenState) {
                            AuthScreenState.LOGIN -> {
                                LoginScreen(
                                    onNavigateToRegister = { screenState = AuthScreenState.REGISTER },
                                    onLoginSuccess = { token, name ->
                                        authToken = token
                                        userName = name
                                        screenState = AuthScreenState.HOME
                                    }
                                )
                            }
                            AuthScreenState.REGISTER -> {
                                RegisterScreen(
                                    onNavigateToLogin = { screenState = AuthScreenState.LOGIN },
                                    onRegisterSuccess = { token, name ->
                                        authToken = token
                                        userName = name
                                        screenState = AuthScreenState.HOME
                                    }
                                )
                            }
                            AuthScreenState.HOME -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Welcome, $userName!", style = MaterialTheme.typography.headlineMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        authToken = null
                                        userName = ""
                                        screenState = AuthScreenState.LOGIN
                                    }) {
                                        Text("Log Out")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}