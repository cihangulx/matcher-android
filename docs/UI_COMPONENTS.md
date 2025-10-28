# UI Bileşenleri

Matcher Android uygulamasının kullanıcı arayüzü bileşenleri ve tasarım sistemi.

> **📱 Toast Sistemi**: Bildirim mesajları için detaylı kullanım kılavuzu için [Toast Sistemi](../TOAST_USAGE_EXAMPLES.md) sayfasına bakın.

## 🎨 Tema Sistemi

### Font Ailesi
```kotlin
val Poppins = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)
```

### Typography
```kotlin
private val AppTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Poppins),
        displayMedium = base.displayMedium.copy(fontFamily = Poppins),
        displaySmall = base.displaySmall.copy(fontFamily = Poppins),
        headlineLarge = base.headlineLarge.copy(fontFamily = Poppins),
        headlineMedium = base.headlineMedium.copy(fontFamily = Poppins),
        headlineSmall = base.headlineSmall.copy(fontFamily = Poppins),
        titleLarge = base.titleLarge.copy(fontFamily = Poppins),
        titleMedium = base.titleMedium.copy(fontFamily = Poppins),
        titleSmall = base.titleSmall.copy(fontFamily = Poppins),
        bodyLarge = base.bodyLarge.copy(fontFamily = Poppins),
        bodyMedium = base.bodyMedium.copy(fontFamily = Poppins),
        bodySmall = base.bodySmall.copy(fontFamily = Poppins),
        labelLarge = base.labelLarge.copy(fontFamily = Poppins),
        labelMedium = base.labelMedium.copy(fontFamily = Poppins),
        labelSmall = base.labelSmall.copy(fontFamily = Poppins),
    )
}
```

### AppTheme
```kotlin
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = AppTypography,
        content = content
    )
}
```

## 🧩 Custom UI Bileşenleri

### 1. ImagePickerBottomSheet
```kotlin
@Composable
fun ImagePickerBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    // Kamera ve galeri seçenekleri sunan bottom sheet
}
```

### 2. GiftPickerBottomSheet
```kotlin
@Composable
fun GiftPickerBottomSheet(
    gifts: List<Gift>,
    onGiftSelected: (Gift) -> Unit,
    onDismiss: () -> Unit
) {
    // Hediye seçimi için bottom sheet
}
```

### 3. ImagePickerDialog
```kotlin
@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    // Resim seçimi için dialog
}
```

### 4. ImageUploadComponent
```kotlin
@Composable
fun ImageUploadComponent(
    onImageSelected: (Uri) -> Unit,
    onUploadComplete: (String) -> Unit,
    onUploadError: (String) -> Unit
) {
    // Resim yükleme bileşeni
}
```

### 5. Skeleton Views

#### ProfileSkeletonView
```kotlin
@Composable
fun ProfileSkeletonView() {
    // Profil yüklenirken gösterilen skeleton
}
```

#### ConversationSkeletonView
```kotlin
@Composable
fun ConversationSkeletonView() {
    // Konuşma listesi yüklenirken gösterilen skeleton
}
```

#### LikesGridSkeletonView
```kotlin
@Composable
fun LikesGridSkeletonView() {
    // Beğeniler grid'i yüklenirken gösterilen skeleton
}
```

## 📱 Ana Ekran Bileşenleri

### MainScreen
```kotlin
@Composable
fun MainScreen() {
    val selectedTab = remember { mutableStateOf(HomeTab.Discover) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab içeriği
        when (selectedTab.value) {
            HomeTab.Discover -> HomeScreen()
            HomeTab.Chats -> MessagesScreen()
            HomeTab.Likes -> LikesScreen()
            HomeTab.Profile -> AccountScreen()
        }
        
        // Alt navigasyon
        BottomNavigation(selectedTab = selectedTab.value) { tab ->
            selectedTab.value = tab
        }
    }
}
```

### Tab Navigation
```kotlin
private enum class HomeTab(
    val title: String,
    val iconRes: Int,
    val iconSelectedRes: Int
) {
    Discover("Keşfet", R.drawable.menu_item_1, R.drawable.menu_item_1_selected),
    Chats("Sohbetler", R.drawable.menu_item_2, R.drawable.menu_item_2_selected),
    Likes("Beğeniler", R.drawable.menu_item_3, R.drawable.menu_item_3_selected),
    Profile("Profil", R.drawable.menu_item_4, R.drawable.menu_item_4_selected)
}
```

## 🔐 Kimlik Doğrulama Bileşenleri

### LoginScreen
```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    // Giriş formu
    // Email ve şifre alanları
    // Google ile giriş butonu
    // Kayıt ol ve şifremi unuttum linkleri
}
```

### AccountSelectScreen
```kotlin
@Composable
fun AccountSelectScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Hesap seçimi ekranı
    // Giriş yap ve kayıt ol butonları
}
```

### RegisterScreen
```kotlin
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Kayıt formu
    // Kişisel bilgiler
    // Kullanım şartları onayı
}
```

## 👤 Profil Bileşenleri

### ProfileScreen
```kotlin
@Composable
fun ProfileScreen(
    user: User,
    onEditProfile: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Profil bilgileri
    // Galeri görüntüleme
    // Düzenle ve ayarlar butonları
}
```

### UpdateProfileScreen
```kotlin
@Composable
fun UpdateProfileScreen(
    user: User,
    onSave: (User) -> Unit,
    onCancel: () -> Unit
) {
    // Profil düzenleme formu
    // Kişisel bilgi alanları
    // Galeri yönetimi
}
```

### GalleryContent
```kotlin
@Composable
fun GalleryContent(
    images: List<GalleryImage>,
    onAddImage: () -> Unit,
    onRemoveImage: (GalleryImage) -> Unit,
    onSetMainImage: (GalleryImage) -> Unit
) {
    // Galeri grid'i
    // Resim ekleme/çıkarma
    // Ana resim belirleme
}
```

## 💬 Mesajlaşma Bileşenleri

### MessagesScreen
```kotlin
@Composable
fun MessagesScreen(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit
) {
    // Konuşma listesi
    // Arama çubuğu
    // Filtreleme seçenekleri
}
```

### ChatScreen
```kotlin
@Composable
fun ChatScreen(
    conversation: Conversation,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onSendGift: (Gift) -> Unit
) {
    // Mesaj listesi
    // Mesaj gönderme alanı
    // Hediye gönderme
}
```

## 💰 Cüzdan Bileşenleri

### WalletScreen
```kotlin
@Composable
fun WalletScreen(
    wallet: Wallet,
    onPurchaseTokens: () -> Unit,
    onPurchaseVip: () -> Unit
) {
    // Cüzdan bilgileri
    // Token bakiyesi
    // VIP durumu
    // Satın alma butonları
}
```

### VipScreen
```kotlin
@Composable
fun VipScreen(
    vipPackages: List<TokenPackage>,
    onPurchaseVip: (TokenPackage) -> Unit
) {
    // VIP paketleri
    // Fiyatlandırma
    // Satın alma butonları
}
```

### DiscountScreen
```kotlin
@Composable
fun DiscountScreen(
    discountPackages: List<TokenPackage>,
    onPurchaseDiscount: (TokenPackage) -> Unit
) {
    // İndirimli paketler
    // Kupon kodu girişi
    // Satın alma butonları
}
```

## ⚙️ Ayarlar Bileşenleri

### SecurityScreen
```kotlin
@Composable
fun SecurityScreen(
    securitySettings: SecuritySettings,
    onUpdateSettings: (SecuritySettings) -> Unit
) {
    // Güvenlik ayarları
    // Şifre değiştirme
    // İki faktörlü doğrulama
}
```

### SettingsActivity
```kotlin
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // Ayarlar ekranı
            }
        }
    }
}
```

## 🎁 Hediye Bileşenleri

### GiftPickerBottomSheet
```kotlin
@Composable
fun GiftPickerBottomSheet(
    gifts: List<Gift>,
    onGiftSelected: (Gift) -> Unit,
    onDismiss: () -> Unit
) {
    // Hediye seçimi
    // Hediye listesi
    // Seçim onayı
}
```

## 🔧 Utility Bileşenleri

### FilterPanel
```kotlin
@Composable
fun FilterPanel(
    filter: ProfileFilter,
    onFilterChanged: (ProfileFilter) -> Unit,
    onApplyFilter: () -> Unit
) {
    // Filtre seçenekleri
    // Yaş aralığı
    // Konum filtresi
    // Uygula butonu
}
```

## 📱 Responsive Design

### Ekran Boyutları
```kotlin
@Composable
fun ResponsiveLayout(
    content: @Composable (BoxScope.() -> Unit)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        content()
    }
}
```

### Adaptive Padding
```kotlin
@Composable
fun AdaptivePadding(
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    
    val horizontalPadding = when {
        screenWidth < 600.dp -> 16.dp
        screenWidth < 840.dp -> 24.dp
        else -> 32.dp
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
    ) {
        content()
    }
}
```

## 🎨 Animasyonlar

### Lottie Animasyonları
```kotlin
@Composable
fun LoadingAnimation() {
    LottieAnimation(
        composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading)).value,
        modifier = Modifier.size(100.dp)
    )
}
```

### Transition Animasyonları
```kotlin
@Composable
fun AnimatedVisibility(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        content()
    }
}
```

## 🔄 State Management

### Remember State
```kotlin
@Composable
fun StatefulComponent() {
    var isLoading by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf<List<Item>?>(null) }
    
    // Component logic
}
```

### ViewModel Integration
```kotlin
@Composable
fun ViewModelComponent(
    viewModel: MyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Component logic
}
```

Bu UI bileşenleri yapısı, uygulamanın tutarlı ve kullanıcı dostu bir arayüze sahip olmasını sağlar.
