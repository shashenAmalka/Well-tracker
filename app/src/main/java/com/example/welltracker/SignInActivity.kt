package com.example.welltracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.databinding.ActivitySignInBinding

/**
 * SignInActivity - Handles user authentication and login
 * Users can sign in with their email and password, or navigate to registration
 */
class SignInActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignInBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SharedPreferencesManager
        sharedPreferencesManager = SharedPreferencesManager(this)
        
        // Check if user is already logged in
        if (sharedPreferencesManager.isUserLoggedIn()) {
            navigateToMainActivity()
            return
        }
        
        setupClickListeners()
        setupBackPressHandler()
    }
    
    /**
     * Setup back press handler
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If user came from onboarding or registration, exit app instead of going back
                finishAffinity()
            }
        })
    }
    
    /**
     * Setup click listeners for buttons and text views
     */
    private fun setupClickListeners() {
        // Sign In button click listener
        binding.signInButton.setOnClickListener {
            handleSignIn()
        }
        
        // Register text click listener
        binding.registerText.setOnClickListener {
            navigateToRegisterActivity()
        }
        
        // Skip login text click listener (optional - for demo purposes)
        binding.skipLoginText?.setOnClickListener {
            // For demo purposes, allow skipping login
            Toast.makeText(this, "Skipping login - Demo mode", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        }
    }
    
    /**
     * Handle sign in process
     * Validates input and attempts to authenticate user
     */
    private fun handleSignIn() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        
        // Validate input
        if (!validateInput(email, password)) {
            return
        }
        
        // Attempt to sign in
        if (sharedPreferencesManager.signInUser(email, password)) {
            Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show()
            // Clear password field for security
            binding.passwordEditText.text?.clear()
        }
    }
    
    /**
     * Validate user input
     * @param email User's email
     * @param password User's password
     * @return true if input is valid, false otherwise
     */
    private fun validateInput(email: String, password: String): Boolean {
        // Clear previous errors
        binding.emailEditText.error = null
        binding.passwordEditText.error = null
        
        // Validate email
        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            binding.emailEditText.requestFocus()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Please enter a valid email address"
            binding.emailEditText.requestFocus()
            return false
        }
        
        // Validate password
        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            binding.passwordEditText.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            binding.passwordEditText.requestFocus()
            return false
        }
        
        return true
    }
    
    /**
     * Navigate to RegisterActivity
     */
    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * Navigate to MainActivity and clear back stack
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}