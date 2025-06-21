package com.example.motoagora

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UserProfileScreen() {
    val user = SessionManager.currentUser

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user != null) {
            Text("Bem-vindo, ${user.name}!", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Seu status de cadastro é:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            val statusColor = when (user.status) {
                "aprovado" -> Color(0xFF388E3C)
                "reprovado" -> Color(0xFFD32F2F)
                else -> Color.Blue
            }
            val statusText = user.status.replaceFirstChar { it.uppercase() }

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge,
                color = statusColor
            )

            val explanationText = when(user.status) {
                "pendente" -> "Seu perfil está em análise. Avisaremos assim que for aprovado."
                "reprovado" -> "Seu cadastro não foi aprovado. Por favor, entre em contato com o suporte para mais detalhes."
                else -> ""
            }

            if (explanationText.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(explanationText, textAlign = TextAlign.Center)
            }

        } else {
            Text("Erro: não foi possível carregar os dados do usuário.")
        }
    }
}
