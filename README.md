# Matcher Android

Modern ve kullanıcı dostu bir eşleşme uygulaması.

## 🚀 Hızlı Başlangıç

### Gereksinimler
- Android Studio Arctic Fox veya üzeri
- JDK 8 veya üzeri
- Android SDK 24+ (API Level 24+)

### Kurulum
```bash
git clone <repository-url>
cd Matcher
./gradlew build
```

### Çalıştırma
```bash
./gradlew installDebug
```

## 📚 Dokümantasyon

Detaylı dokümantasyon için [docs/](./docs/) klasörünü inceleyin:

### 🏗️ Temel Dokümantasyon
- [Kurulum Kılavuzu](./docs/SETUP.md) - Projeyi nasıl kuracağınız
- [Proje Mimarisi](./docs/ARCHITECTURE.md) - Genel mimari yapı
- [UI Bileşenleri](./docs/UI_COMPONENTS.md) - Custom UI bileşenleri

### 🔌 Entegrasyonlar
- [API Entegrasyonu](./docs/API_INTEGRATION.md) - Backend API bağlantıları
- [Socket.IO](./docs/SOCKET_INTEGRATION.md) - Gerçek zamanlı mesajlaşma
- [Ödeme Sistemi](./docs/PAYMENT_INTEGRATION.md) - Google Play Billing entegrasyonu

### 🎨 UI/UX
- [Toast Sistemi](./docs/TOAST_USAGE_EXAMPLES.md) - Bildirim sistemi kullanım kılavuzu

### 📖 Tüm Dokümantasyonlar
Tüm mevcut dokümantasyonlar için [Dokümantasyon İndeksi](./docs/README.md) sayfasına bakın.

## 🏗️ Proje Yapısı

```
app/src/main/java/com.flort.evlilik.
├── main/                    # Ana uygulama sınıfları
│   ├── MainActivity.kt      # Ana aktivite
│   └── MatcherApplication.kt # Uygulama sınıfı
├── modules/                 # Uygulama modülleri
│   ├── auth/               # Kimlik doğrulama
│   ├── account/            # Hesap yönetimi
│   ├── main/               # Ana ekran ve alt sekmeler
│   ├── splash/             # Splash ekranı
│   ├── terms/              # Kullanım şartları
│   ├── wallet/             # Cüzdan ve ödeme
│   └── crop/               # Resim kırpma
├── models/                  # Veri modelleri
│   ├── auth/               # Kimlik doğrulama modelleri
│   ├── message/            # Mesaj modelleri
│   ├── profile/            # Profil modelleri
│   ├── user/               # Kullanıcı modelleri
│   └── packages/           # Paket modelleri
├── network/                 # Ağ katmanı
│   ├── service/            # API servisleri
│   ├── socket/             # Socket.IO yönetimi
│   └── repository/         # Veri repository'leri
├── utils/                   # Yardımcı sınıflar
│   ├── helpers/            # Helper sınıfları
│   └── events/             # Event yönetimi
└── components/              # UI bileşenleri
```

## 🛠️ Teknolojiler

- **Kotlin** - Ana programlama dili
- **Jetpack Compose** - Modern UI framework
- **Retrofit** - HTTP client
- **Socket.IO** - Gerçek zamanlı iletişim
- **OneSignal** - Push bildirimleri
- **Google Play Billing** - Ödeme sistemi
- **Coil** - Resim yükleme
- **DataStore** - Veri saklama
- **uCrop** - Resim kırpma

## 📱 Özellikler

- ✅ Kullanıcı kayıt ve giriş (Email, Google)
- ✅ Profil yönetimi ve galeri
- ✅ Eşleşme sistemi (Beğeni/Beğenmeme)
- ✅ Gerçek zamanlı mesajlaşma
- ✅ Push bildirimleri
- ✅ Ödeme sistemi (Token, VIP)
- ✅ Güvenlik ayarları
- ✅ Kullanıcı engelleme
- ✅ Hediye gönderme
- ✅ Rapor sistemi

## 🔧 Build Konfigürasyonu

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 36
- **Java Version**: 1.8
