package com.example.welltracker.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.welltracker.SignInActivity
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.databinding.FragmentSettingsBinding
import com.example.welltracker.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var prefsStore: PrefsStore
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        // Initialize managers
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        prefsStore = PrefsStore(requireContext())
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
        updateUserInfo()
        
        viewModel.loadSettings()
    }
    
    /**
     * Update user information display
     */
    private fun updateUserInfo() {
        val userData = sharedPreferencesManager.getCurrentUserData()
        if (userData != null) {
            binding.userNameText.text = userData.username
            binding.userEmailText.text = userData.email
        } else {
            // Fallback if no user data (shouldn't happen in normal flow)
            binding.userNameText.text = "Guest User"
            binding.userEmailText.text = "Not logged in"
        }
    }
    
    private fun setupObservers() {
        viewModel.notificationsEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.switchNotifications.isChecked = enabled
        })
        
        viewModel.darkMode.observe(viewLifecycleOwner, Observer { enabled ->
            binding.switchDarkMode.isChecked = enabled
        })
    }
    
    private fun setupClickListeners() {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
            Toast.makeText(context, "Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
        
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkMode(isChecked)
            Toast.makeText(context, "Dark mode ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
        
        // Help & Support click listener
        binding.helpSupportLayout.setOnClickListener {
            Toast.makeText(context, "Help & Support clicked", Toast.LENGTH_SHORT).show()
        }
        
        // Log Out click listener with confirmation dialog
        binding.logoutLayout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    
    /**
     * Show logout confirmation dialog
     */
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Perform logout operation
     * This clears ALL user-specific data to ensure complete isolation
     */
    private fun performLogout() {
        // Get current user ID before clearing session
        val currentUserId = sharedPreferencesManager.getCurrentUserEmail()
        
        // Clear user-specific data from PrefsStore (habits, moods, hydration)
        if (currentUserId != null) {
            prefsStore.clearUserData(currentUserId)
        }
        
        // Clear user session from SharedPreferencesManager
        sharedPreferencesManager.logoutUser()
        
        // Show logout message
        Toast.makeText(context, "Logged out successfully. All your data has been cleared.", Toast.LENGTH_SHORT).show()
        
        // Navigate to SignInActivity and clear back stack
        val intent = Intent(requireContext(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // Finish the current activity to prevent back navigation
        requireActivity().finish()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
