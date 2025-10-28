# Kurulum Kılavuzu

Bu dokümanda Matcher Android projesini nasıl kuracağınızı adım adım öğreneceksiniz.

> **📱 Toast Sistemi**: Kurulum sırasında hata mesajları için [Toast Sistemi](../TOAST_USAGE_EXAMPLES.md) sayfasına bakın.

## 📋 Gereksinimler

### Sistem Gereksinimleri
- **İşletim Sistemi**: Windows 10+, macOS 10.14+, Ubuntu 18.04+
- **RAM**: En az 8GB (16GB önerilen)
- **Disk Alanı**: En az 10GB boş alan
- **İnternet Bağlantısı**: SDK indirmek için gerekli

### Yazılım Gereksinimleri
- **Android Studio**: Arctic Fox (2020.3.1) veya üzeri
- **JDK**: OpenJDK 11 veya Oracle JDK 11
- **Android SDK**: API Level 21+ (Android 5.0+)
- **Git**: Proje klonlamak için

## 🚀 Kurulum Adımları

### 1. Android Studio Kurulumu

1. [Android Studio'yu indirin](https://developer.android.com/studio)
2. İndirilen dosyayı çalıştırın
3. Kurulum sihirbazını takip edin
4. SDK Manager'dan gerekli SDK'ları indirin:
   - Android SDK Platform 21-33
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
   - Android SDK Tools

### 2. JDK Kurulumu

#### Windows:
```bash
# Chocolatey ile
choco install openjdk11

# Veya manuel indirme
# https://adoptium.net/ adresinden JDK 11 indirin
```

#### macOS:
```bash
# Homebrew ile
brew install openjdk@11

# Veya manuel indirme
# https://adoptium.net/ adresinden JDK 11 indirin
```

#### Ubuntu/Debian:
```bash
sudo apt update
sudo apt install openjdk-11-jdk
```

### 3. Proje Klonlama

```bash
# Projeyi klonlayın
git clone <repository-url>
cd Matcher

# Submodule'ları güncelleyin (varsa)
git submodule update --init --recursive
```

### 4. Android Studio'da Proje Açma

1. Android Studio'yu açın
2. "Open an existing Android Studio project" seçin
3. Klonladığınız `Matcher` klasörünü seçin
4. "Trust Project" butonuna tıklayın

### 5. Gradle Sync

1. Android Studio otomatik olarak Gradle sync başlatacak
2. Eğer başlamazsa, "Sync Project with Gradle Files" butonuna tıklayın
3. Sync tamamlanana kadar bekleyin

### 6. SDK ve Build Tools Kontrolü

1. `File > Project Structure` menüsünü açın
2. `SDK Location` sekmesine gidin
3. Aşağıdaki yolların doğru olduğundan emin olun:
   - Android SDK location
   - JDK location
   - Android NDK location (gerekirse)

## 🔧 Yapılandırma

### 1. local.properties Dosyası

Proje kök dizininde `local.properties` dosyası oluşturun:

```properties
# Windows için
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# macOS için
sdk.dir=/Users/YourUsername/Library/Android/sdk

# Linux için
sdk.dir=/home/YourUsername/Android/Sdk
```

### 2. Gradle Wrapper İzinleri (Linux/macOS)

```bash
chmod +x gradlew
```

### 3. Build Konfigürasyonu

`app/build.gradle.kts` dosyasında aşağıdaki ayarları kontrol edin:

```kotlin
android {
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        // ...
    }
}
```

## 🧪 Kurulum Testi

### 1. Proje Build Testi

```bash
# Terminal'de proje dizininde
./gradlew build

# Veya Android Studio'da
# Build > Make Project
```

### 2. APK Oluşturma Testi

```bash
# Debug APK oluştur
./gradlew assembleDebug

# Release APK oluştur (signing gerekli)
./gradlew assembleRelease
```

### 3. Emulator Testi

1. Android Studio'da `Tools > AVD Manager` açın
2. Yeni bir Virtual Device oluşturun
3. API Level 21+ seçin
4. Emulator'ü başlatın
5. Uygulamayı çalıştırın

## 🔍 Sorun Giderme

### Yaygın Sorunlar

#### 1. Gradle Sync Hatası
```
Error: Could not find method compile()
```
**Çözüm**: `build.gradle` dosyalarında `compile` yerine `implementation` kullanın.

#### 2. SDK Bulunamadı Hatası
```
SDK location not found
```
**Çözüm**: `local.properties` dosyasını kontrol edin ve doğru SDK yolunu girin.

#### 3. JDK Hatası
```
Unsupported Java version
```
**Çözüm**: JDK 11 kullandığınızdan emin olun.

#### 4. Memory Hatası
```
OutOfMemoryError: Java heap space
```
**Çözüm**: `gradle.properties` dosyasına ekleyin:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
```

### Log Kontrolü

```bash
# Detaylı build log'u
./gradlew build --info

# Debug log'u
./gradlew build --debug
```

## 📱 Cihaz Testi

### 1. USB Debugging

1. Android cihazınızda `Developer Options` açın
2. `USB Debugging` aktif edin
3. Cihazı bilgisayara bağlayın
4. "Allow USB Debugging" onayını verin

### 2. Cihazda Çalıştırma

```bash
# Debug APK yükle
./gradlew installDebug

# Veya Android Studio'da
# Run > Run 'app'
```

## 🔄 Güncelleme

### Proje Güncelleme

```bash
# En son değişiklikleri çek
git pull origin main

# Gradle wrapper güncelle
./gradlew wrapper --gradle-version=7.5

# Dependencies güncelle
./gradlew dependencies --write-locks
```

### Android Studio Güncelleme

1. `Help > Check for Updates` menüsünü açın
2. Güncellemeleri indirin ve yükleyin
3. Projeyi yeniden açın

## 📞 Yardım

Kurulum sırasında sorun yaşarsanız:

1. [Issues](https://github.com/your-repo/issues) sayfasına bakın
2. Yeni issue oluşturun
3. Hata log'larını paylaşın
4. Sistem bilgilerinizi belirtin

## ✅ Kurulum Kontrol Listesi

- [ ] Android Studio kuruldu
- [ ] JDK 11 kuruldu
- [ ] Android SDK kuruldu
- [ ] Proje klonlandı
- [ ] Gradle sync tamamlandı
- [ ] Build başarılı
- [ ] Emulator çalışıyor
- [ ] Cihazda test edildi

Kurulum tamamlandı! Artık [Hızlı Başlangıç](./QUICK_START.md) kılavuzuna geçebilirsiniz.
