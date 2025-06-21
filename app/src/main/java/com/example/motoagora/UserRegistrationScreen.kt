package com.example.motoagora

import android.content.ContentResolver
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: UserRegistrationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UserRegistrationViewModel(context.contentResolver) as T
            }
        }
    )

    val name by viewModel.name
    val email by viewModel.email
    val password by viewModel.password
    val age by viewModel.age
    val maritalStatus by viewModel.maritalStatus
    val cpf by viewModel.cpf
    val phoneNumber by viewModel.phoneNumber
    val cidade by viewModel.cidade
    val cnhPhotoUri by viewModel.cnhPhotoUri
    val motoDocUri by viewModel.motoDocUri
    val profilePhotoUri by viewModel.profilePhotoUri
    val registrationState by viewModel.registrationState.collectAsState()

    val maritalStatusOptions = listOf("Solteiro(a)", "Casado(a)", "Divorciado(a)")
    val cityOptions = listOf("Colider-MT", "Sorriso-MT", "Sinop-MT")
    var maritalStatusExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(registrationState) {
        when (val state = registrationState) {
            is RegistrationState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                navController.navigate("auth") { popUpTo("auth") { inclusive = true } }
            }
            is RegistrationState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    val cnhPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.cnhPhotoUri.value = uri }
    val motoDocPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.motoDocUri.value = uri }
    val profilePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.profilePhotoUri.value = uri }

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = Color.White,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.White,
        unfocusedIndicatorColor = Color.White.copy(alpha = 0.7f),
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Cadastro de Motorista", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { viewModel.name.value = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                OutlinedTextField(value = email, onValueChange = { viewModel.email.value = it }, label = { Text("E-mail") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.password.value = it },
                    label = { Text("Senha") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Mostrar/Esconder Senha", tint = Color.White)
                        }
                    },
                    colors = textFieldColors
                )
                OutlinedTextField(value = age, onValueChange = { viewModel.age.value = it }, label = { Text("Idade") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = textFieldColors)

                ExposedDropdownMenuBox(
                    expanded = maritalStatusExpanded,
                    onExpandedChange = { maritalStatusExpanded = !maritalStatusExpanded },
                ) {
                    OutlinedTextField(
                        value = maritalStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado Civil") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = textFieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = maritalStatusExpanded,
                        onDismissRequest = { maritalStatusExpanded = false }
                    ) {
                        maritalStatusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.maritalStatus.value = option
                                    maritalStatusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = cpf,
                    onValueChange = {
                        if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                            viewModel.cpf.value = it
                        }
                    },
                    label = { Text("CPF") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CpfVisualTransformation(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                            viewModel.phoneNumber.value = it
                        }
                    },
                    label = { Text("Telefone (com DDD)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = PhoneNumberVisualTransformation(),
                    colors = textFieldColors
                )

                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = !cityExpanded },
                ) {
                    OutlinedTextField(
                        value = cidade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sua Cidade") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = textFieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        cityOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.cidade.value = option
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { profilePhotoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = BorderStroke(1.dp, Color.White)) { Text(if (profilePhotoUri != null) "✓ Foto de Perfil Selecionada" else "Selecionar Foto de Perfil") }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { cnhPhotoPicker.launch("image/*") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = BorderStroke(1.dp, Color.White)) { Text(if (cnhPhotoUri != null) "✓ CNH Anexada" else "Anexar CNH") }
                    OutlinedButton(onClick = { motoDocPicker.launch("image/*") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = BorderStroke(1.dp, Color.White)) { Text(if (motoDocUri != null) "✓ Doc. Anexado" else "Anexar Doc. Moto") }
                }
                Spacer(Modifier.height(16.dp))
                if (registrationState is RegistrationState.Loading) { CircularProgressIndicator(color = Color.White) } else { Button(onClick = { viewModel.registerUser() }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Finalizar Cadastro", fontWeight = FontWeight.Bold) } }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}