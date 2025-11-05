package com.flort.evlilik.modules.auth.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.flort.evlilik.R

class GoogleShimmerButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val contentContainer = FrameLayout(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(60))
        background = GradientDrawable().apply {
            cornerRadius = dp(12).toFloat()
            setColor(Color.parseColor("#302DFF"))
        }
        clipToOutline = true
        setPadding(0, 0, 0, 0)
    }

    private val iconView = ImageView(context).apply {
        setImageResource(R.drawable.google_logo)
        layoutParams = LayoutParams(dp(24), dp(24), Gravity.START or Gravity.CENTER_VERTICAL).apply {
            marginStart = dp(16)
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private val textView = TextView(context).apply {
        setTextColor(Color.WHITE)
        text = "Google ile giriş yap"
        textSize = 16f
        typeface = resources.getFont(R.font.poppins_medium)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
    }

    private val shimmerView = View(context).apply {
        setBackgroundResource(R.drawable.shimmer_gradient)
        layoutParams = LayoutParams(dp(120), LayoutParams.MATCH_PARENT)
        alpha = 0.5f
        isClickable = false
        visibility = VISIBLE
    }

    init {
        addView(contentContainer)

        val innerContent = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(dp(16), 0, dp(16), 0)
        }
        contentContainer.addView(innerContent)

        innerContent.addView(textView)
        innerContent.addView(iconView)

        contentContainer.addView(shimmerView)
        shimmerView.bringToFront()

        contentContainer.isClickable = true
        contentContainer.isFocusable = true

        post { startShimmer() }
    }
    
    /**
     * Google butonu tıklama listener'ı
     */
    fun setOnGoogleClickListener(listener: () -> Unit) {
        contentContainer.setOnClickListener {
            listener()
        }
    }

    private fun startShimmer() {
        shimmerView.updateLayoutParams<LayoutParams> {
            height = contentContainer.height
        }
        val width = contentContainer.width
        shimmerView.translationX = -shimmerView.width.toFloat()
        shimmerView.animate()
            .translationX(width + shimmerView.width.toFloat())
            .setDuration(1500)
            .setInterpolator(LinearInterpolator())
            .withEndAction { postDelayed({ startShimmer() }, 0) }
            .start()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
