package com.example.motoagora

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController, viewModel: AdminViewModel = viewModel()) {
    val context = LocalContext.current
    val pendingUsers by viewModel.pendingUsers.collectAsState()
    val approvedUsers by viewModel.approvedUsers.collectAsState()
    val rejectedUsers by viewModel.rejectedUsers.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showCreateRideDialog by viewModel.showCreateRideDialog.collectAsState()
    val startLocationAddress by viewModel.startLocationAddress.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pendentes", "Aprovados", "Reprovados")

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onOpenCreateRideDialog(context)
            }
        }
    )

    LaunchedEffect(key1 = true) {
        viewModel.fetchDrivers()
    }

    // Mostra Toast para mensagens de sucesso ou erro
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel de Administrador") },
                actions = {
                    // NOVO BOTÃO PARA PARAR AS CORRIDAS
                    IconButton(onClick = { viewModel.stopSimulatedRides() }) {
                        Icon(Icons.Default.StopCircle, contentDescription = "Parar Corridas Pendentes")
                    }
                    TextButton(onClick = {
                        viewModel.logout()
                        navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                    }) {
                        Text("Sair")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Criar Corrida")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            if (showCreateRideDialog) {
                CreateRideDialog(
                    viewModel = viewModel,
                    startLocation = startLocationAddress,
                    onStartLocationChange = { viewModel.startLocationAddress.value = it }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> UserList(users = pendingUsers, listType = "pendente", viewModel = viewModel)
                    1 -> UserList(users = approvedUsers, listType = "aprovado", viewModel = viewModel)
                    2 -> UserList(users = rejectedUsers, listType = "reprovado", viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRideDialog(
    viewModel: AdminViewModel,
    startLocation: String,
    onStartLocationChange: (String) -> Unit
) {
    var endLocation by remember { mutableStateOf("") }
    val paymentOptions = listOf("Dinheiro", "Pix", "Cartão")
    var paymentMethod by remember { mutableStateOf(paymentOptions[0]) }
    var paymentExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.showCreateRideDialog.value = false },
        title = { Text("Criar Nova Corrida") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startLocation,
                    onValueChange = onStartLocationChange,
                    label = { Text("Localização do Cliente") }
                )
                OutlinedTextField(
                    value = endLocation,
                    onValueChange = { endLocation = it },
                    label = { Text("Destino do Cliente") }
                )

                Button(onClick = { /* TODO: Implementar tela de mapa */ }, enabled = false) {
                    Text("Selecionar Destino no Mapa")
                }

                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Forma de Pagamento") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = paymentExpanded, onDismissRequest = { paymentExpanded = false }) {
                        paymentOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    paymentMethod = option
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }
                Text("Valor da Corrida: R$ 7,00", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.createRide(startLocation, endLocation, paymentMethod)
            }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showCreateRideDialog.value = false }) { Text("Cancelar") }
        }
    )
}

@Composable
fun UserList(users: List<UserModel>, listType: String, viewModel: AdminViewModel) {
    if (users.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Nenhum motorista nesta categoria.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                UserCard(
                    user = user,
                    showActions = listType == "pendente",
                    onApproveClick = { viewModel.approveDriver(user.id) },
                    onReproveClick = { viewModel.reproveDriver(user.id) }
                )
            }
        }
    }
}

@Composable
fun UserCard(
    user: UserModel,
    showActions: Boolean,
    onApproveClick: () -> Unit,
    onReproveClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text("CPF: ${user.cpf}", style = MaterialTheme.typography.bodyMedium)
            Text("Telefone: ${user.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("Cidade: ${user.cidade}", style = MaterialTheme.typography.bodyMedium)
            Text("E-mail: ${user.email}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            if (user.cnhPhotoUrl.isNotBlank()) {
                Text(
                    text = "Ver Foto da CNH",
                    modifier = Modifier.clickable { uriHandler.openUri(user.cnhPhotoUrl) },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }
            if (user.motoDocUrl.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Ver Doc. da Moto",
                    modifier = Modifier.clickable { uriHandler.openUri(user.motoDocUrl) },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }
            Spacer(Modifier.height(8.dp))
            val statusColor = when (user.status) {
                "aprovado" -> Color(0xFF388E3C)
                "reprovado" -> Color(0xFFD32F2F)
                else -> Color.Blue
            }

            Text(
                "Status: ${user.status.replaceFirstChar { it.uppercase() }}",
                color = statusColor,
                style = MaterialTheme.typography.bodyMedium
            )

            if (showActions) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onApproveClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Aprovar")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Aprovar")
                    }
                    Button(
                        onClick = onReproveClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Reprovar")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Reprovar")
                    }
                }
            }
        }
    }
}