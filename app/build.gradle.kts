plugins {
    id("com.google.gms.google-services") // Mantenha este plugin no topo
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.motoagora"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.motoagora"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- DEPENDÊNCIAS PRINCIPAIS DO ANDROID E JETPACK COMPOSE ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Dependência para serviços de localização do Google
    implementation("com.google.android.gms:play-services-location:21.3.0")

// Dependência para carregar imagens da internet (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")


    // --- DEPENDÊNCIAS DO FIREBASE (ORGANIZADAS) ---
    // Importa o Bill of Materials (BoM) para gerenciar as versões
    implementation(platform("com.google.firebase:firebase-bom:33.15.0")) // Use a versão do artefato anterior

    // Adicione as bibliotecas do Firebase que seu app precisa
    implementation("com.google.firebase:firebase-auth")      // Para Autenticação
    implementation("com.google.firebase:firebase-firestore")  // Para o banco de dados Firestore
    implementation("com.google.firebase:firebase-storage")    // Para o armazenamento de arquivos (fotos)

    // Biblioteca para usar 'await()' com as tarefas do Firebase (MUITO IMPORTANTE)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Dependência para Geocoding (converter coordenadas em endereço) com OSM
    //implementation("org.osmdroid:osmdroid-bonuspack:6.9.0")


    // --- DEPENDÊNCIAS DE TESTE ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Dependências para Retrofit (rede) e Gson (conversão de JSON)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // Dependência para Socket.IO (comunicação em tempo real)
    implementation("io.socket:socket.io-client:2.1.0")
}