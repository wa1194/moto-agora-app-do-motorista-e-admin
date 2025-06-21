package com.example.motoagora

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMapScreen(navController: NavController, viewModel: DriverMapViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.startServiceIfNeeded(context)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController) {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Painel do Motorista") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir Menu")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            viewModel.logout(context)
                            navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                        }) {
                            Text("Deslogar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            MapContent(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun MapContent(modifier: Modifier, navController: NavController, viewModel: DriverMapViewModel) {
    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsState()
    val earnings by viewModel.earnings.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val recenterMapTrigger by viewModel.recenterMap.collectAsState()
    val newRide by viewModel.newRideAvailable.collectAsState()
    val acceptedRide by viewModel.acceptedRide.collectAsState()
    var hasCenteredOnFirstLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startLocationUpdates(context)
            }
        }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.toggleOnlineStatus(context)
            } else {
                Toast.makeText(context, "Permissão de notificação é necessária para ficar online.", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(newRide) {
        if (newRide != null) {
            viewModel.playRideNotificationSound(context)
        }
    }

    LaunchedEffect(acceptedRide) {
        if (acceptedRide != null) {
            navController.navigate("accepted_ride_screen")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(5.0)
                    controller.setCenter(GeoPoint(-14.235, -51.925))
                }
            },
            update = { mapView ->
                currentLocation?.let { geoPoint ->
                    if (!hasCenteredOnFirstLocation) {
                        mapView.controller.animateTo(geoPoint, 18.0, 1000)
                        hasCenteredOnFirstLocation = true
                    }
                    if (recenterMapTrigger > 0) {
                        mapView.controller.animateTo(geoPoint)
                    }
                    mapView.overlays.clear()
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }
            }
        )

        newRide?.let { ride ->
            NewRideNotificationDialog(
                ride = ride,
                // AQUI ESTÁ A CORREÇÃO: Passando o 'context' para a função
                onAccept = { viewModel.acceptRide(context) },
                onDecline = { viewModel.declineRide() }
            )
        }

        FloatingActionButton(
            onClick = { viewModel.onCenterMapClick() },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = "Centralizar Localização")
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            EarningsAndStatusPanel(isOnline = isOnline, earnings = earnings) {
                navController.navigate("earnings_screen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (!isOnline && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.toggleOnlineStatus(context)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnline) Color(0xFFD32F2F) else Color(0xFF388E3C)
                )
            ) {
                Text(
                    text = if (isOnline) "Ficar Offline" else "Ficar Online",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EarningsAndStatusPanel(isOnline: Boolean, earnings: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF388E3C) else Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Ganhos de Hoje",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "R$ ${"%.2f".format(earnings)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(navController: NavController, onClose: () -> Unit) {
    val user = SessionManager.currentUser
    val context = LocalContext.current
    val denuciaNumber = "5566996276217"
    val denuciaLink = "https://wa.me/$denuciaNumber?text=Preciso%20fazer%20uma%20denúncia."

    ModalDrawerSheet {
        if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(user.profilePhotoUrl),
                    contentDescription = "Foto do Motorista",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                Text(user.cidade, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Avisos") },
            selected = false,
            onClick = { /* Navegar para tela de avisos */ onClose() }
        )
        NavigationDrawerItem(
            label = { Text("Meus Ganhos") },
            selected = false,
            onClick = {
                navController.navigate("earnings_screen")
                onClose()
            }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Suporte", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        NavigationDrawerItem(
            label = { Text("Fale Conosco") },
            icon = { Icon(Icons.Default.ContactSupport, contentDescription = null) },
            selected = false,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:walterap994@gmail.com")
                }
                context.startActivity(Intent.createChooser(intent, "Enviar E-mail"))
                onClose()
            }
        )
        NavigationDrawerItem(
            label = { Text("Denúncia", color = Color.Red) },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            selected = false,
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(denuciaLink)
                        setPackage("com.whatsapp")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "WhatsApp não instalado.", Toast.LENGTH_SHORT).show()
                }
                onClose()
            }
        )
    }
}

@Composable
fun NewRideNotificationDialog(ride: RideModel, onAccept: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Não pode ser dispensado clicando fora */ },
        title = { Text("Nova Corrida Disponível!", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("De: ${ride.startLocation}")
                Text("Para: ${ride.endLocation}")
                Text("Pagamento: ${ride.paymentMethod}")
                Text("Valor: R$ ${"%.2f".format(ride.value)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        confirmButton = {
            Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                Text("Aceitar")
            }
        },
        dismissButton = {
            Button(onClick = onDecline, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Recusar")
            }
        }
    )
}
