# Proje Mimarisi

Matcher Android uygulamasının mimari yapısı ve tasarım prensipleri.

> **📱 Toast Sistemi**: Uygulama genelinde kullanılan bildirim sistemi için [Toast Sistemi](../TOAST_USAGE_EXAMPLES.md) sayfasına bakın.

## 🏗️ Genel Mimari

Matcher uygulaması **Activity + Compose** mimarisini kullanır ve **Repository Pattern** ile veri yönetimi yapar.

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│  Activities  │  Jetpack Compose Screens  │  ViewModels     │
├─────────────────────────────────────────────────────────────┤
│                    Data Layer                               │
├─────────────────────────────────────────────────────────────┤
│  Repositories  │  ApiClient  │  SocketManager  │  Models   │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Klasör Yapısı

```
app/src/main/java/com.flort.evlilik.
├── main/                      # Ana uygulama sınıfları
│   ├── MainActivity.kt        # Ana aktivite
│   └── MatcherApplication.kt  # Uygulama sınıfı
├── modules/                   # Uygulama modülleri
│   ├── auth/                 # Kimlik doğrulama
│   │   ├── LoginScreen.kt    # Giriş ekranı
│   │   ├── AccountSelectScreen.kt # Hesap seçimi
│   │   ├── register/         # Kayıt modülü
│   │   └── forgot/           # Şifre sıfırlama
│   ├── account/              # Hesap yönetimi
│   │   ├── SecurityScreen.kt # Güvenlik ayarları
│   │   ├── UpdateProfileScreen.kt # Profil güncelleme
│   │   └── GalleryContent.kt # Galeri yönetimi
│   ├── main/                 # Ana ekran
│   │   ├── MainScreen.kt     # Ana ekran
│   │   ├── tabs/             # Alt sekmeler
│   │   ├── profile/          # Profil ekranları
│   │   └── message/          # Mesaj ekranları
│   ├── wallet/               # Cüzdan ve ödeme
│   │   ├── WalletScreen.kt   # Cüzdan ekranı
│   │   ├── VipScreen.kt      # VIP ekranı
│   │   └── DiscountScreen.kt # İndirim ekranı
│   ├── splash/               # Splash ekranı
│   ├── terms/                # Kullanım şartları
│   └── crop/                 # Resim kırpma
├── models/                   # Veri modelleri
│   ├── auth/                 # Kimlik doğrulama modelleri
│   ├── message/              # Mesaj modelleri
│   ├── profile/              # Profil modelleri
│   ├── user/                 # Kullanıcı modelleri
│   ├── packages/             # Paket modelleri
│   ├── gift/                 # Hediye modelleri
│   └── ticket/               # Rapor modelleri
├── network/                  # Ağ katmanı
│   ├── ApiClient.kt          # API istemcisi
│   ├── Routes.kt             # API endpoint'leri
│   ├── service/              # API servisleri
│   ├── socket/               # Socket.IO yönetimi
│   └── repository/           # Veri repository'leri
├── utils/                    # Yardımcı sınıflar
│   ├── helpers/              # Helper sınıfları
│   │   ├── ToastHelper.kt    # Toast yönetimi
│   │   ├── BillingHelper.kt  # Ödeme yönetimi
│   │   └── OneSignalHelper.kt # Push bildirim yönetimi
│   ├── events/               # Event yönetimi
│   ├── PreferencesManager.kt # Veri saklama
│   └── Theme.kt              # Tema yönetimi
└── components/               # UI bileşenleri
    ├── ImagePickerBottomSheet.kt
    ├── GiftPickerBottomSheet.kt
    └── ImageUploadComponent.kt
```

## 🔄 Veri Akışı

### 1. UI → ApiClient → Repository

```kotlin
// UI Layer
@Composable
fun ProfileScreen() {
    var profile by remember { mutableStateOf<Profile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val apiClient = ApiClient.getInstance(LocalContext.current)
            val response = apiClient.authService.profile()
            if (response.success && response.data != null) {
                profile = response.data
            }
        } catch (e: Exception) {
            // Hata yönetimi
        } finally {
            isLoading = false
        }
    }
    
    if (isLoading) {
        LoadingIndicator()
    } else {
        profile?.let { ProfileContent(profile = it) }
    }
}

// Repository
class ProfileRepository(private val context: Context) {
    private val apiClient = ApiClient.getInstance(context)
    
    suspend fun getProfile(): Result<Profile> {
        return try {
            val response = apiClient.authService.profile()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 🧩 Modül Yapısı

### Auth Modülü
- **LoginScreen**: Giriş ekranı
- **RegisterScreen**: Kayıt ekranı
- **ForgotPasswordScreen**: Şifre sıfırlama
- **AuthViewModel**: Kimlik doğrulama mantığı

### Profile Modülü
- **ProfileScreen**: Profil görüntüleme
- **EditProfileScreen**: Profil düzenleme
- **GalleryContent**: Fotoğraf galerisi
- **ProfileViewModel**: Profil yönetimi

### Messaging Modülü
- **ChatListScreen**: Mesaj listesi
- **ChatScreen**: Mesajlaşma ekranı
- **MessageViewModel**: Mesaj yönetimi
- **SocketManager**: Gerçek zamanlı iletişim

## 🔌 Dependency Management

**Singleton Pattern** kullanarak dependency yönetimi:

```kotlin
@SuppressLint("StaticFieldLeak")
class ApiClient private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null
        
        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiClient(context).also { INSTANCE = it }
            }
        }
    }
    
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    
    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
}
```

## 🌐 Ağ Katmanı

### Retrofit + OkHttp
```kotlin
interface ApiService {
    @GET("profile")
    suspend fun getProfile(): ProfileResponse
    
    @POST("profile")
    suspend fun updateProfile(@Body profile: ProfileRequest): ProfileResponse
}

class ApiClient @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) {
    suspend fun getProfile(): Result<Profile> {
        return try {
            val response = apiService.getProfile()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Socket.IO Entegrasyonu
```kotlin
class SocketManager @Inject constructor() {
    private var socket: Socket? = null
    
    fun connect(token: String) {
        socket = IO.socket("$BASE_URL?token=$token")
        socket?.connect()
    }
    
    fun sendMessage(message: Message) {
        socket?.emit("send_message", message.toJson())
    }
    
    fun onNewMessage(callback: (Message) -> Unit) {
        socket?.on("new_message") { args ->
            val message = Message.fromJson(args[0].toString())
            callback(message)
        }
    }
}
```

## 💾 Veri Yönetimi

### DataStore Preferences
```kotlin
class PreferencesManager private constructor(context: Context) {
    private val dataStore = context.dataStore
    
    val userToken: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[USER_TOKEN_KEY]
        }
    
    suspend fun saveUserToken(token: String) {
        dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context).also { INSTANCE = it }
            }
        }
        
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
    }
}
```

## 🎨 UI Mimarisi

### Jetpack Compose
```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            profile?.let { profile ->
                ProfileContent(profile = profile)
            }
        }
    }
}

@Composable
private fun ProfileContent(profile: Profile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "${profile.age} yaşında",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

## 🔄 State Management

### Compose State
```kotlin
@Composable
fun ProfileScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<Profile?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            profile = loadProfile()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }
    
    if (isLoading) {
        LoadingIndicator()
    } else if (error != null) {
        ErrorMessage(error = error!!)
    } else {
        profile?.let { ProfileContent(profile = it) }
    }
}
```


## 📊 Performans Optimizasyonları

### 1. Lazy Loading
```kotlin
@Composable
fun ProfileList(profiles: List<Profile>) {
    LazyColumn {
        items(profiles) { profile ->
            ProfileItem(profile = profile)
        }
    }
}
```

### 2. Image Loading
```kotlin
@Composable
fun ProfileImage(imageUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Profile Image",
        modifier = Modifier.size(100.dp),
        contentScale = ContentScale.Crop
    )
}
```

### 3. Memory Management
```kotlin
@Composable
fun ProfileScreen() {
    DisposableEffect(Unit) {
        onDispose {
            // Cleanup resources
        }
    }
}
```


Bu mimari yapı, uygulamanın ölçeklenebilir, test edilebilir ve sürdürülebilir olmasını sağlar. Her katman kendi sorumluluğuna odaklanır ve diğer katmanlarla gevşek bağlıdır.
