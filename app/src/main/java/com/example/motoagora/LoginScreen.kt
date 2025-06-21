package com.example.motoagora

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val loginIdentifier by viewModel.loginIdentifier
    val password by viewModel.password
    val rememberMe by viewModel.rememberMe
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    // Estado para controlar a visibilidade da senha
    var passwordVisible by remember { mutableStateOf(false) }

    // Carrega as credenciais salvas quando a tela é iniciada
    LaunchedEffect(Unit) {
        viewModel.loadSavedCredentials(context)
    }

    // Observa o estado do login para navegar ou mostrar erros
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.UserSuccess -> {
                Toast.makeText(context, "Bem-vindo, ${state.user.name}!", Toast.LENGTH_SHORT).show()
                viewModel.handleRememberMe(context) // Salva ou limpa as credenciais
                navController.navigate("user_profile") { popUpTo("auth") { inclusive = true } }
            }
            is LoginState.AdminSuccess -> {
                Toast.makeText(context, "Bem-vindo, Admin!", Toast.LENGTH_SHORT).show()
                viewModel.handleRememberMe(context) // Salva ou limpa as credenciais
                navController.navigate("admin_panel") { popUpTo("auth") { inclusive = true } }
            }
            is LoginState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Acesse sua conta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = loginIdentifier,
            onValueChange = { viewModel.loginIdentifier.value = it },
            label = { Text("Email ou CPF") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Ícone de usuário") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password.value = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Ícone de cadeado") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            // Lógica para mostrar/esconder a senha
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { viewModel.rememberMe.value = it }
            )
            Text(
                text = "Lembrar-me",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        if (loginState is LoginState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.performLogin() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Entrar")
            }
        }
    }
}