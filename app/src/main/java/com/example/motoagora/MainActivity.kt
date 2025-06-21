package com.example.motoagora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motoagora.ui.theme.MotoAgoraTheme

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDestination = "auth"

    // ViewModel compartilhado para as telas do motorista
    val driverMapViewModel: DriverMapViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth") {
            AuthScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("user_registration") {
            UserRegistrationScreen(navController)
        }
        composable("admin_panel") {
            AdminScreen(navController = navController)
        }
        composable("user_profile") {
            val user = SessionManager.currentUser
            if (user?.status == "aprovado") {
                LaunchedEffect(true) {
                    navController.navigate("driver_map_screen") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            } else {
                UserProfileScreen()
            }
        }
        composable("driver_map_screen") {
            DriverMapScreen(navController, driverMapViewModel)
        }
        composable("earnings_screen") {
            EarningsScreen()
        }
        // Rota para a nova tela, passando o ViewModel compartilhado
        composable("accepted_ride_screen") {
            AcceptedRideScreen(viewModel = driverMapViewModel)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotoAgoraTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}
