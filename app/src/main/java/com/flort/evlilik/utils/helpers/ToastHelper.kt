package com.flort.evlilik.utils.helpers

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.flort.evlilik.R

/**
 * Global Toast Helper - Custom Implementation
 *
 * Bu sınıf tüm uygulama genelinde kullanılabilecek, okunabilir ve modern
 * toast mesajları için merkezi bir yönetim sağlar.
 *
 * Kullanım örnekleri:
 * - ToastHelper.showSuccess(activity, "İşlem başarılı!")
 * - ToastHelper.showError(activity, "Bir hata oluştu!")
 * - ToastHelper.showWarning(activity, "Uyarı mesajı")
 * - ToastHelper.showInfo(activity, "Bilgi mesajı")
 * - ToastHelper.showDelete(activity, "Silindi!")
 *
 * Toast renklerini ve stilini değiştirmek için sadece bu helper'ı düzenlemeniz yeterlidir.
 */
object ToastHelper {

    // Toast süreleri
    const val SHORT_DURATION = Toast.LENGTH_SHORT
    const val LONG_DURATION = Toast.LENGTH_LONG

    // Toast pozisyonları
    const val GRAVITY_TOP = Gravity.TOP
    const val GRAVITY_CENTER = Gravity.CENTER
    const val GRAVITY_BOTTOM = Gravity.BOTTOM

    // Varsayılan değerler
    private const val DEFAULT_DURATION = Toast.LENGTH_LONG
    private const val DEFAULT_GRAVITY = Gravity.TOP

    /**
     * Toast tipleri ve renk şemaları
     */
    private enum class ToastType(
        val backgroundColor: String,
        val iconRes: Int,
        val titleColor: String,
        val messageColor: String
    ) {
        SUCCESS(
            backgroundColor = "#E8F8F5", // Açık yeşil-beyaz
            iconRes = R.drawable.ic_toast_success,
            titleColor = "#1E8449",
            messageColor = "#27AE60"
        ),
        ERROR(
            backgroundColor = "#FADBD8", // Açık kırmızı-beyaz
            iconRes = R.drawable.ic_toast_error,
            titleColor = "#A93226",
            messageColor = "#C0392B"
        ),
        WARNING(
            backgroundColor = "#FEF5E7", // Açık sarı-beyaz
            iconRes = R.drawable.ic_toast_warning,
            titleColor = "#D68910",
            messageColor = "#F39C12"
        ),
        INFO(
            backgroundColor = "#EBF5FB", // Açık mavi-beyaz
            iconRes = R.drawable.ic_toast_info,
            titleColor = "#1F618D",
            messageColor = "#2E86C1"
        ),
        DELETE(
            backgroundColor = "#F2F3F4", // Açık gri-beyaz
            iconRes = R.drawable.ic_toast_delete,
            titleColor = "#515A5A",
            messageColor = "#707B7C"
        )
    }

    /**
     * Başarı toast'ı gösterir (Açık yeşil tema)
     */
    fun showSuccess(
        activity: Activity,
        message: String,
        title: String = "Başarılı ✓",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) {
        showToast(activity, title, message, ToastType.SUCCESS, duration, gravity)
    }

    /**
     * Hata toast'ı gösterir (Açık kırmızı tema)
     */
    fun showError(
        activity: Activity,
        message: String,
        title: String = "Hata!",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) {
        showToast(activity, title, message, ToastType.ERROR, duration, gravity)
    }

    /**
     * Uyarı toast'ı gösterir (Açık sarı tema)
     */
    fun showWarning(
        activity: Activity,
        message: String,
        title: String = "Uyarı!",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) {
        showToast(activity, title, message, ToastType.WARNING, duration, gravity)
    }

    /**
     * Bilgi toast'ı gösterir (Açık mavi tema)
     */
    fun showInfo(
        activity: Activity,
        message: String,
        title: String = "Bilgi",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) {
        showToast(activity, title, message, ToastType.INFO, duration, gravity)
    }

    /**
     * Silme toast'ı gösterir (Açık gri tema)
     */
    fun showDelete(
        activity: Activity,
        message: String,
        title: String = "Silindi",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) {
        showToast(activity, title, message, ToastType.DELETE, duration, gravity)
    }

    /**
     * Ana toast gösterme fonksiyonu
     */
    private fun showToast(
        activity: Activity,
        title: String,
        message: String,
        type: ToastType,
        duration: Int,
        gravity: Int
    ) {
        try {
            activity.runOnUiThread {
                val inflater = LayoutInflater.from(activity)
                val layout = inflater.inflate(R.layout.custom_toast_layout, null)

                // Container background
                val container = layout.findViewById<LinearLayout>(R.id.toast_container)
                val cardView = layout.findViewById<CardView>(R.id.toast_card)
                cardView.setCardBackgroundColor(Color.parseColor(type.backgroundColor))

                // Icon
                val icon = layout.findViewById<ImageView>(R.id.toast_icon)
                icon.setImageResource(type.iconRes)

                // Title
                val titleView = layout.findViewById<TextView>(R.id.toast_title)
                titleView.text = title
                titleView.setTextColor(Color.parseColor(type.titleColor))

                // Message
                val messageView = layout.findViewById<TextView>(R.id.toast_message)
                messageView.text = message
                messageView.setTextColor(Color.parseColor(type.messageColor))

                // Create and show toast
                val toast = Toast(activity)
                toast.duration = Toast.LENGTH_SHORT
                toast.view = layout

                // Set gravity with offset - kenarlara yapışık
                when (gravity) {
                    Gravity.TOP -> toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)
                    Gravity.CENTER -> toast.setGravity(Gravity.CENTER or Gravity.FILL_HORIZONTAL, 0, 0)
                    else -> toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
                }

                toast.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default toast
            Toast.makeText(activity, message, duration).show()
        }
    }

    // ============= Dark Theme Variants (Aynı fonksiyonlar) =============

    /**
     * Dark tema için - şimdilik normal temayı kullanıyor
     * Gerekirse farklı renkler eklenebilir
     */
    fun showSuccessDark(
        activity: Activity,
        message: String,
        title: String = "Başarılı ✓",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) = showSuccess(activity, message, title, duration, gravity)

    fun showErrorDark(
        activity: Activity,
        message: String,
        title: String = "Hata!",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) = showError(activity, message, title, duration, gravity)

    fun showWarningDark(
        activity: Activity,
        message: String,
        title: String = "Uyarı!",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) = showWarning(activity, message, title, duration, gravity)

    fun showInfoDark(
        activity: Activity,
        message: String,
        title: String = "Bilgi",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) = showInfo(activity, message, title, duration, gravity)

    fun showDeleteDark(
        activity: Activity,
        message: String,
        title: String = "Silindi",
        duration: Int = DEFAULT_DURATION,
        gravity: Int = DEFAULT_GRAVITY
    ) = showDelete(activity, message, title, duration, gravity)
}