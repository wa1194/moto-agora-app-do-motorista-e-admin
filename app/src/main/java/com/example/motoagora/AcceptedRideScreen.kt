package com.example.motoagora

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptedRideScreen(viewModel: DriverMapViewModel) {
    val acceptedRide by viewModel.acceptedRide.collectAsState()
    val context = LocalContext.current
    var chatMessage by remember { mutableStateOf("") }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Tenta iniciar a chamada novamente se a permissão for concedida
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:${acceptedRide?.clientPhoneNumber}")
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Permissão para fazer ligações é necessária.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Limpa a corrida aceita da memória quando o usuário sai desta tela
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAcceptedRide()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Corrida em Andamento") })
        }
    ) { paddingValues ->
        acceptedRide?.let { ride ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                RideDetailsCard(ride = ride)
                Spacer(modifier = Modifier.height(16.dp))
                ChatAndActions(
                    chatMessage = chatMessage,
                    onMessageChange = { chatMessage = it },
                    onSendMessage = { /* TODO: Implementar envio de chat */ },
                    onCallClient = {
                        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                    }
                )
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma corrida em andamento.")
            }
        }
    }
}

@Composable
fun RideDetailsCard(ride: RideModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Detalhes da Corrida", style = MaterialTheme.typography.titleLarge)
            HorizontalDivider()
            Text("Buscar em:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(ride.startLocation, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Levar para:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(ride.endLocation, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pagamento:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(ride.paymentMethod, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valor:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("R$ ${"%.2f".format(ride.value)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ChatAndActions(
    chatMessage: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onCallClient: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Chat com o Cliente", style = MaterialTheme.typography.titleMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("O chat com o cliente ainda não foi implementado.", color = Color.Gray)
        }
        OutlinedTextField(
            value = chatMessage,
            onValueChange = onMessageChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Digite sua mensagem...") },
            trailingIcon = {
                IconButton(onClick = onSendMessage, enabled = false) { // Desabilitado por enquanto
                    Icon(Icons.Default.Send, contentDescription = "Enviar Mensagem")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onCallClient,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = "Ligar para o cliente")
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Ligar para o Cliente")
        }
    }
}
