package com.example.motoagora

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen() {
    // ViewModel pode ser adicionada depois para buscar dados reais
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Ganhos Semanais", "Ganhos Mensais")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Ganhos") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Conteúdo baseado na aba
            when (selectedTabIndex) {
                0 -> WeeklyEarningsContent()
                1 -> MonthlyEarningsContent()
            }
        }
    }
}

@Composable
fun WeeklyEarningsContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Selecione a Semana", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        // Lógica para selecionar semana virá aqui
        Text("Dados de ganhos semanais aparecerão aqui.", fontSize = 16.sp)
    }
}

@Composable
fun MonthlyEarningsContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Selecione o Mês", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        // Lógica para selecionar mês virá aqui
        Text("Dados de ganhos mensais aparecerão aqui.", fontSize = 16.sp)
    }
}