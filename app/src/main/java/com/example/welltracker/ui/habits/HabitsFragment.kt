package com.example.welltracker.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.welltracker.databinding.FragmentHabitsBinding
import com.example.welltracker.viewmodel.HabitsViewModel
import com.example.welltracker.data.PrefsStore

class HabitsFragment : Fragment() {
    
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HabitsViewModel by viewModels()
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var prefsStore: PrefsStore
    
    private val waterGlasses = mutableListOf<ImageView>()
    private var currentWaterCount = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsStore = PrefsStore(requireContext())
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        // Water tracking removed - now handled by HydrationFragment
        // setupWaterTracker()
        
        viewModel.loadHabits()
        // loadWaterProgress()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload habits when fragment becomes visible again
        // This ensures the list is updated when returning from other screens
        viewModel.loadHabits()
    }
    
    private fun setupWaterTracker() {
        // Water tracking removed - now handled by HydrationFragment
        /*
        // Initialize water glass ImageViews
        waterGlasses.clear()
        waterGlasses.addAll(listOf(
            binding.glass1, binding.glass2, binding.glass3, binding.glass4,
            binding.glass5, binding.glass6, binding.glass7, binding.glass8
        ))
        
        // Set click listeners for each glass
        waterGlasses.forEachIndexed { index, glass ->
            glass.setOnClickListener {
                toggleWaterGlass(index + 1)
            }
        }
        */
    }
    
    private fun toggleWaterGlass(glassNumber: Int) {
        // Water tracking removed - now handled by HydrationFragment
        /*
        if (glassNumber <= currentWaterCount) {
            // Remove water from this glass and all after it
            currentWaterCount = glassNumber - 1
        } else {
            // Add water up to this glass
            currentWaterCount = glassNumber
        }
        
        updateWaterDisplay()
        saveWaterProgress()
        */
    }
    
    private fun updateWaterDisplay() {
        // Water tracking removed - now handled by HydrationFragment
        /*
        waterGlasses.forEachIndexed { index, glass ->
            if (index < currentWaterCount) {
                glass.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(requireContext(), com.example.welltracker.R.color.primary_blue)
                )
            } else {
                glass.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(requireContext(), com.example.welltracker.R.color.primary_blue_light)
                )
            }
        }
        
        binding.tvWaterCount.text = "$currentWaterCount/8 glasses"
        */
    }
    
    private fun saveWaterProgress() {
        // Water tracking removed - now handled by HydrationFragment
        // prefsStore.setWaterCount(currentWaterCount)
    }
    
    private fun loadWaterProgress() {
        // Water tracking removed - now handled by HydrationFragment
        // currentWaterCount = prefsStore.getWaterCount()
        // updateWaterDisplay()
    }
    
    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onToggleComplete = { habit ->
                viewModel.toggleHabitCompletion(habit.id)
            },
            onDelete = { habit ->
                viewModel.deleteHabit(habit.id)
                Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = habitAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.habits.observe(viewLifecycleOwner, Observer { habits ->
            habitAdapter.updateHabits(habits)
        })
        
        viewModel.todayProgress.observe(viewLifecycleOwner, Observer { progress ->
            val (completed, total) = progress
            binding.tvProgressCount.text = "$completed/$total"
            
            val progressPercentage = if (total > 0) {
                (completed * 100) / total
            } else {
                0
            }
            binding.progressBar.progress = progressPercentage
            binding.tvProgressPercentage.text = "$progressPercentage%"
        })
    }
    
    private fun setupClickListeners() {
        binding.btnAddNewHabit.setOnClickListener {
            showAddHabitDialog()
        }
        
        // View All habits - scroll to show all habits in the list
        binding.tvViewAll.setOnClickListener {
            // First, ensure we have the latest data
            viewModel.loadHabits()
            
            // Wait for the data to load, then scroll
            binding.recyclerViewHabits.post {
                val totalHabits = habitAdapter.itemCount
                if (totalHabits > 0) {
                    // Scroll to show all habits - go to the middle first, then to the end
                    binding.recyclerViewHabits.smoothScrollToPosition(0)
                    binding.recyclerViewHabits.postDelayed({
                        binding.recyclerViewHabits.smoothScrollToPosition(totalHabits - 1)
                    }, 300)
                    
                    // Show visual feedback with breakdown
                    val completedCount = habitAdapter.getCompletedCount()
                    val message = "Showing all $totalHabits habits ($completedCount completed)"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                } else {
                    // Handle empty state - reload data first
                    viewModel.loadHabits()
                    Toast.makeText(context, "No habits found. Add your first habit below!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Quick access cards
        binding.cardLogMood.setOnClickListener {
            // Navigate to mood fragment
            navigateToFragment(com.example.welltracker.ui.mood.MoodFragment())
        }
        
        binding.cardViewAnalysis.setOnClickListener {
            // Navigate to analysis fragment
            navigateToFragment(com.example.welltracker.ui.analysis.AnalysisFragment())
        }
        
        // Home button navigation
        binding.cardHome.setOnClickListener {
            navigateToFragment(com.example.welltracker.ui.home.HomeFragment())
        }
    }
    
    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(com.example.welltracker.R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        
        // Update bottom navigation selection
        updateBottomNavigation(fragment)
    }
    
    private fun updateBottomNavigation(fragment: Fragment) {
        val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            com.example.welltracker.R.id.bottom_navigation
        )
        
        bottomNav?.selectedItemId = when (fragment) {
            is com.example.welltracker.ui.home.HomeFragment -> com.example.welltracker.R.id.nav_home
            is com.example.welltracker.ui.mood.MoodFragment -> com.example.welltracker.R.id.nav_mood
            is com.example.welltracker.ui.analysis.AnalysisFragment -> com.example.welltracker.R.id.nav_settings
            else -> com.example.welltracker.R.id.nav_habits
        }
    }
    
    private fun showAddHabitDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(com.example.welltracker.R.layout.dialog_add_habit, null)
        
        val etHabitName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.welltracker.R.id.et_habit_name)
        val etHabitGoal = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.welltracker.R.id.et_habit_goal)
        
        dialog.setView(dialogView)
            .setTitle("Add New Habit")
            .setPositiveButton("Add") { _, _ ->
                val name = etHabitName.text.toString().trim()
                val goal = etHabitGoal.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    viewModel.addHabit(name, goal)
                    
                    // Explicitly reload habits to ensure UI is updated
                    viewModel.loadHabits()
                    
                    // Scroll to the new habit after a short delay to ensure the view is updated
                    binding.recyclerViewHabits.post {
                        if (habitAdapter.itemCount > 0) {
                            binding.recyclerViewHabits.smoothScrollToPosition(habitAdapter.itemCount - 1)
                        }
                    }
                    
                    android.widget.Toast.makeText(context, "Habit added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Please enter a habit name", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
