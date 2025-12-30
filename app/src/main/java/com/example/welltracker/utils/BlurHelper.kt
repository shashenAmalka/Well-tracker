package com.example.welltracker.utils

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity

class BlurHelper {
    companion object {
        private const val TAG = "BlurHelper"
        private const val DEFAULT_BLUR_RADIUS = 20f
        private const val DEFAULT_OVERLAY_COLOR = "#15FFFFFF"
        
        fun setupBlurView(
            activity: FragmentActivity, 
            blurView: View,
            radius: Float = DEFAULT_BLUR_RADIUS,
            overlayColor: String = DEFAULT_OVERLAY_COLOR
        ) {
            Log.d(TAG, "BlurView setup temporarily disabled - using fallback background")
            try {
                blurView.setBackgroundColor(Color.parseColor(overlayColor))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set fallback background", e)
            }
        }
    }
}
