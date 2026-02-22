package com.arsenii.fitnessapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

// Base for all activities
// Handles the background
open class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applyBackground()
    }

    // Applies the background to the view
    private fun applyBackground() {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView.setBackgroundResource(R.drawable.bck)
    }
}
