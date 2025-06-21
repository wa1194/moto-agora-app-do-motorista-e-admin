# Moto Agora (App do Motorista & Admin)

Aplicativo nativo Android que serve como ferramenta de trabalho para os motoristas e painel de controle para os administradores da plataforma "Moto Agora".

## ✨ Funcionalidades

### Perfil do Motorista
- **Cadastro Completo:** Interface para o motorista se cadastrar na plataforma, enviando nome, dados pessoais e documentos (fotos da CNH, documento da moto e perfil).
- **Login e Status:** Autenticação segura e tela de perfil que exibe o status do cadastro (`Pendente`, `Aprovado`, `Reprovado`).
- **Painel Principal (Mapa):**
    - Exibição do mapa com a localização atual do motorista.
    - Botão para ficar `Online` ou `Offline`.
    - Notificação em tempo real sobre novas corridas quando está online.
    - Diálogo para `Aceitar` ou `Recusar` uma nova corrida.
- **Serviço em Primeiro Plano:** Mantém o motorista conectado e recebendo corridas mesmo com o app em segundo plano, exibindo uma notificação persistente.

### Perfil do Administrador
- **Login de Admin:** Acesso seguro ao painel de gerenciamento.
- **Painel de Controle:**
    - Visualização de motoristas dividida em abas: `Pendentes`, `Aprovados` e `Reprovados`.
    - Detalhes de cada motorista, incluindo links para visualizar os documentos enviados.
    - Ações rápidas para `Aprovar` ou `Reprovar` um cadastro pendente com um clique.

## 🛠️ Tecnologias e Arquitetura

- **Linguagem:** 100% [Kotlin](https://kotlinlang.org/), seguindo as diretrizes da Google.
- **Interface Gráfica:** [Jetpack Compose](https://developer.android.com/jetpack/compose) para uma UI moderna e reativa.
- **Arquitetura:** MVVM (Model-View-ViewModel) para separação de responsabilidades e melhor testabilidade.
- **Comunicação com API:**
    - [Retrofit](https://square.github.io/retrofit/): Cliente HTTP para consumir a API do backend.
    - [OkHttp](https://square.github.io/okhttp/): Usado pelo Retrofit para as chamadas de rede.
- **Comunicação em Tempo Real:** Biblioteca oficial do [Socket.IO para Java/Android](https://github.com/socketio/socket.io-client-java).
- **Assincronismo:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html) para gerenciar tarefas em background.
- **Mapas:** [osmdroid](https://github.com/osmdroid/osmdroid), uma alternativa de código aberto para mapas no Android.

## ⚙️ Configuração para Desenvolvimento

1.  **Pré-requisitos:**
    - Ter o [Android Studio](https://developer.android.com/studio) instalado.
    - Ter o [servidor do Moto App](https://github.com/seu-usuario/servidor-moto-app) rodando localmente ou em um servidor acessível.

2.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/seu-usuario/moto-agora-motorista.git](https://github.com/seu-usuario/moto-agora-motorista.git)
    ```

3.  **Abra no Android Studio:**
    - Abra o Android Studio.
    - Selecione `Open` e navegue até a pasta do projeto clonado.

4.  **Configure a URL do Backend:**
    - Abra o arquivo `app/src/main/java/com/example/motoagora/RetrofitInstance.kt`.
    - Altere a constante `BASE_URL` para o endereço do seu servidor.
    ```kotlin
    // Exemplo para servidor local (use o IP da sua máquina, não localhost)
    private const val BASE_URL = "[http://192.168.1.10:3000/](http://192.168.1.10:3000/)"
    ```

5.  **Compile e Execute:**
    - Conecte um dispositivo Android ou inicie um emulador.
    - Clique no botão `Run 'app'` no Android Studio.

