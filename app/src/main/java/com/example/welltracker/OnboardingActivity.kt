package com.example.welltracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.databinding.ActivityOnboardingBinding

/**
 * OnboardingActivity - App introduction and welcome screen
 * Shows app features and navigates to SignInActivity
 */
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SharedPreferencesManager
        sharedPreferencesManager = SharedPreferencesManager(this)
        
        // Check if user is already logged in
        if (sharedPreferencesManager.isUserLoggedIn()) {
            navigateToMainActivity()
            return
        }
        
        setupClickListeners()
    }
    
    /**
     * Setup click listeners for buttons
     */
    private fun setupClickListeners() {
        // Get Started button click listener
        binding.getStartedButton.setOnClickListener {
            navigateToSignInActivity()
        }
        
        // Skip button click listener
        binding.skipButton.setOnClickListener {
            navigateToSignInActivity()
        }
    }
    
    /**
     * Navigate to SignInActivity
     */
    private fun navigateToSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    /**
     * Navigate to MainActivity (if already logged in)
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}