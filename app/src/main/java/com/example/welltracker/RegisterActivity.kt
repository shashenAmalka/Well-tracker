package com.example.welltracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.databinding.ActivityRegisterBinding

/**
 * RegisterActivity - Handles new user registration
 * Users can create a new account with username, email, and password
 */
class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SharedPreferencesManager
        sharedPreferencesManager = SharedPreferencesManager(this)
        
        setupClickListeners()
        setupBackPressHandler()
    }
    
    /**
     * Setup back press handler
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToSignInActivity()
            }
        })
    }
    
    /**
     * Setup click listeners for buttons and text views
     */
    private fun setupClickListeners() {
        // Register button click listener
        binding.registerButton.setOnClickListener {
            handleRegistration()
        }
        
        // Sign in text click listener
        binding.signInText.setOnClickListener {
            navigateToSignInActivity()
        }
    }
    
    /**
     * Handle user registration process
     * Validates input and attempts to register new user
     */
    private fun handleRegistration() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        
        // Validate input
        if (!validateInput(username, email, password)) {
            return
        }
        
        // Attempt to register user
        if (sharedPreferencesManager.registerUser(username, email, password)) {
            Toast.makeText(this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show()
            navigateToSignInActivity()
        } else {
            Toast.makeText(this, "User with this email already exists", Toast.LENGTH_LONG).show()
            binding.emailEditText.error = "Email already registered"
            binding.emailEditText.requestFocus()
        }
    }
    
    /**
     * Validate user input
     * @param username User's username
     * @param email User's email
     * @param password User's password
     * @return true if input is valid, false otherwise
     */
    private fun validateInput(username: String, email: String, password: String): Boolean {
        // Clear previous errors
        binding.usernameEditText.error = null
        binding.emailEditText.error = null
        binding.passwordEditText.error = null
        
        // Validate username
        if (username.isEmpty()) {
            binding.usernameEditText.error = "Username is required"
            binding.usernameEditText.requestFocus()
            return false
        }
        
        if (username.length < 3) {
            binding.usernameEditText.error = "Username must be at least 3 characters"
            binding.usernameEditText.requestFocus()
            return false
        }
        
        if (username.length > 20) {
            binding.usernameEditText.error = "Username must be less than 20 characters"
            binding.usernameEditText.requestFocus()
            return false
        }
        
        // Validate username contains only alphanumeric characters and underscores
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            binding.usernameEditText.error = "Username can only contain letters, numbers, and underscores"
            binding.usernameEditText.requestFocus()
            return false
        }
        
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
        
        if (password.length > 50) {
            binding.passwordEditText.error = "Password must be less than 50 characters"
            binding.passwordEditText.requestFocus()
            return false
        }
        
        // Password strength validation (optional but recommended)
        if (!password.matches(Regex(".*[A-Za-z].*"))) {
            binding.passwordEditText.error = "Password must contain at least one letter"
            binding.passwordEditText.requestFocus()
            return false
        }
        
        return true
    }
    
    /**
     * Navigate back to SignInActivity
     */
    private fun navigateToSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}