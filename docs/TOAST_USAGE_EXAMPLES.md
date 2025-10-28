# ToastHelper Kullanım Kılavuzu

Bu dokümanda global toast sisteminin nasıl kullanılacağını bulabilirsiniz.

> **🔗 İlgili Dokümantasyonlar**:
> - [Ana Dokümantasyon](./README.md) - Tüm dokümantasyon indeksi
> - [UI Bileşenleri](./UI_COMPONENTS.md) - Diğer UI bileşenleri
> - [Proje Mimarisi](./ARCHITECTURE.md) - Genel mimari yapı
> - [API Entegrasyonu](./API_INTEGRATION.md) - API hata yönetimi
> - [Ödeme Sistemi](./PAYMENT_INTEGRATION.md) - Ödeme bildirimleri
> - [Socket.IO](./SOCKET_INTEGRATION.md) - Mesaj bildirimleri

## Kurulum

MotionToast kütüphanesi ve `ToastHelper` utility sınıfı projeye eklenmiştir.

## Temel Kullanım

### 1. Success Toast (Başarı Mesajı)

```kotlin
// Activity içinde
ToastHelper.showSuccess(this, "İşlem başarıyla tamamlandı!")

// Özel başlık ile
ToastHelper.showSuccess(
    activity = this,
    message = "Profil güncellendi",
    title = "Harika! ✓"
)

// Özel süre ve pozisyon ile
ToastHelper.showSuccess(
    activity = this,
    message = "Kayıt başarılı",
    title = "Tebrikler!",
    duration = ToastHelper.SHORT_DURATION,
    gravity = ToastHelper.GRAVITY_TOP
)
```

### 2. Error Toast (Hata Mesajı)

```kotlin
ToastHelper.showError(this, "İşlem başarısız oldu!")

ToastHelper.showError(
    activity = this,
    message = "Sunucuya bağlanılamadı",
    title = "Bağlantı Hatası"
)
```

### 3. Warning Toast (Uyarı Mesajı)

```kotlin
ToastHelper.showWarning(this, "Lütfen tüm alanları doldurun")

ToastHelper.showWarning(
    activity = this,
    message = "Bu işlem geri alınamaz",
    title = "Dikkat!"
)
```

### 4. Info Toast (Bilgi Mesajı)

```kotlin
ToastHelper.showInfo(this, "Yeni bir güncelleme mevcut")

ToastHelper.showInfo(
    activity = this,
    message = "Profiliniz %80 tamamlandı",
    title = "İpucu"
)
```

### 5. Delete Toast (Silme Mesajı)

```kotlin
ToastHelper.showDelete(this, "Mesaj silindi")

ToastHelper.showDelete(
    activity = this,
    message = "Hesap kalıcı olarak silindi",
    title = "Silindi"
)
```

## Dark Theme Variants

Dark tema toast'ları için:

```kotlin
ToastHelper.showSuccessDark(this, "İşlem başarılı!")
ToastHelper.showErrorDark(this, "Bir hata oluştu!")
ToastHelper.showWarningDark(this, "Dikkatli olun!")
ToastHelper.showInfoDark(this, "Bilgi mesajı")
ToastHelper.showDeleteDark(this, "Öğe silindi")
```

## Compose Kullanımı

Compose fonksiyonlarında kullanmak için:

```kotlin
@Composable
fun MyScreen() {
    val context = LocalContext.current
    
    Button(onClick = {
        if (context is Activity) {
            ToastHelper.showSuccess(context, "Butona tıklandı!")
        }
    }) {
        Text("Tıkla")
    }
}
```

## Örnek Senaryolar

### Login Screen'de Kullanım

```kotlin
// LoginScreen.kt içinde
fun handleLogin(email: String, password: String) {
    if (email.isEmpty()) {
        ToastHelper.showWarning(this, "E-posta adresinizi girin")
        return
    }
    
    // API çağrısı
    viewModel.login(email, password) { success, error ->
        if (success) {
            ToastHelper.showSuccess(this, "Giriş başarılı! Hoş geldiniz")
            navigateToMain()
        } else {
            ToastHelper.showError(this, error ?: "Giriş başarısız")
        }
    }
}
```

### Profile Update'de Kullanım

```kotlin
// UpdateProfileScreen.kt içinde
fun saveProfile(profile: Profile) {
    if (!validateProfile(profile)) {
        ToastHelper.showWarning(
            activity = this,
            message = "Lütfen tüm zorunlu alanları doldurun",
            title = "Eksik Bilgi"
        )
        return
    }
    
    profileRepository.update(profile) { success ->
        if (success) {
            ToastHelper.showSuccess(this, "Profiliniz güncellendi")
        } else {
            ToastHelper.showError(this, "Profil güncellenemedi")
        }
    }
}
```

### File Upload'da Kullanım

```kotlin
// ImageUploadComponent.kt içinde
fun uploadImage(uri: Uri) {
    ToastHelper.showInfo(this, "Yükleme başladı...")
    
    fileUploadService.upload(uri) { progress, success, error ->
        when {
            success -> {
                ToastHelper.showSuccess(
                    activity = this,
                    message = "Fotoğraf yüklendi",
                    title = "Başarılı ✓"
                )
            }
            error != null -> {
                ToastHelper.showError(
                    activity = this,
                    message = error,
                    title = "Yükleme Hatası"
                )
            }
        }
    }
}
```

### Delete Confirmation'da Kullanım

```kotlin
// AccountScreen.kt içinde
fun deleteAccount() {
    // Önce onay al
    showConfirmationDialog(
        title = "Hesabı Sil",
        message = "Hesabınızı kalıcı olarak silmek istediğinizden emin misiniz?"
    ) { confirmed ->
        if (confirmed) {
            accountRepository.delete { success ->
                if (success) {
                    ToastHelper.showDelete(
                        activity = this,
                        message = "Hesabınız silindi",
                        title = "Güle güle"
                    )
                    navigateToLogin()
                }
            }
        }
    }
}
```

### Socket Mesajlarında Kullanım

```kotlin
// MainActivity.kt içinde
private fun setupSocketListeners() {
    lifecycleScope.launch {
        socketManager.incomingMessages.collect { event ->
            event?.let { 
                // Yeni mesaj geldiğinde bildir
                ToastHelper.showInfo(
                    activity = this@MainActivity,
                    message = "Yeni mesajınız var: ${it.message.senderName}",
                    title = "Yeni Mesaj",
                    duration = ToastHelper.SHORT_DURATION,
                    gravity = ToastHelper.GRAVITY_TOP
                )
            }
        }
    }
}
```

## Özel Toast

Tüm parametreleri manuel kontrol etmek için:

```kotlin
ToastHelper.showCustom(
    activity = this,
    title = "Özel Başlık",
    message = "Özel mesaj içeriği",
    style = MotionToastStyle.SUCCESS,
    duration = ToastHelper.LONG_DURATION,
    gravity = ToastHelper.GRAVITY_CENTER,
    isDark = true
)
```

## Toast Pozisyonları

- `ToastHelper.GRAVITY_TOP` - Ekranın üstünde
- `ToastHelper.GRAVITY_CENTER` - Ekranın ortasında
- `ToastHelper.GRAVITY_BOTTOM` - Ekranın altında (varsayılan)

## Toast Süreleri

- `ToastHelper.SHORT_DURATION` - 2 saniye
- `ToastHelper.LONG_DURATION` - 4 saniye (varsayılan)

## Toast Stilleri Değiştirme

Toast stillerini değiştirmek isterseniz, sadece `ToastHelper.kt` dosyasını düzenleyin.

Örneğin, `createColorToast` yerine `darkToast` kullanmak isterseniz:

```kotlin
// ToastHelper.kt içinde
fun showSuccess(...) {
    MotionToast.darkToast(  // createColorToast yerine
        activity,
        title,
        message,
        MotionToastStyle.SUCCESS,
        ...
    )
}
```

Bu sayede tüm uygulama genelinde toast stili tek yerden değiştirilmiş olur! 🎉

## Önemli Notlar

1. **Activity Context Gereklidir**: MotionToast Activity context'i gerektirir. Compose fonksiyonlarında `LocalContext.current`'i `Activity` olarak cast edin.

2. **Try-Catch Koruması**: Tüm toast fonksiyonları try-catch ile korunmuştur, bu yüzden crash riski yoktur.

3. **Font Kullanımı**: Varsayılan olarak Poppins fontu kullanılır. Değiştirmek için `ToastHelper.kt` içindeki font referanslarını güncelleyin.

4. **Tema Desteği**: Hem açık hem koyu tema için fonksiyonlar mevcuttur. Uygulama temanıza göre uygun olanı kullanın.

## Gelecek İyileştirmeler (Opsiyonel)

- Compose-only bir wrapper eklenebilir
- Vibration/Sound desteği eklenebilir
- Custom icon desteği eklenebilir
- Toast kuyruğu sistemi eklenebilir (birden fazla toast aynı anda gösterilmemesi için)

