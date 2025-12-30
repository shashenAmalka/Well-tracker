package com.example.welltracker.ui.mood

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.welltracker.databinding.FragmentMoodBinding
import com.example.welltracker.viewmodel.MoodsViewModel
import com.example.welltracker.data.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {
    
    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MoodsViewModel by viewModels()
    private lateinit var moodAdapter: MoodAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // Note: Chart and day selectors moved to home page
        animateCardEntrance() // Only animates stats container now
        
        viewModel.loadMoods()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload moods when fragment becomes visible again
        // This ensures the list is updated when returning from other screens
        viewModel.loadMoods()
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter { mood ->
            viewModel.deleteMood(mood.id)
            Toast.makeText(context, "Mood entry deleted", Toast.LENGTH_SHORT).show()
        }
        
        binding.recyclerViewMoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodAdapter
        }
    }
    
    // Removed setupSegmentControl as it's no longer needed
    
    private fun setupObservers() {
        viewModel.moods.observe(viewLifecycleOwner, Observer { moods ->
            moodAdapter.updateMoods(moods)
            updateMoodHistoryUI(moods)
        })
    }
    
    private fun updateMoodHistoryUI(moods: List<MoodEntry>) {
        // Update mood count
        binding.tvMoodCount.text = if (moods.isEmpty()) {
            "0 entries"
        } else if (moods.size == 1) {
            "1 entry"
        } else {
            "${moods.size} entries"
        }
        
        // Show empty state if no moods, otherwise show RecyclerView
        if (moods.isEmpty()) {
            binding.emptyStateMoods.visibility = View.VISIBLE
            binding.moodHistoryContainer.visibility = View.GONE
        } else {
            binding.emptyStateMoods.visibility = View.GONE
            binding.moodHistoryContainer.visibility = View.VISIBLE
            
            // Update mood statistics only
            updateMoodStatistics(moods)
        }
    }
    
    private fun updateMoodStatistics(moods: List<MoodEntry>) {
        // Stats elements moved to home page
        // Just log the statistics for debugging purposes
        if (moods.isEmpty()) return
        
        // Calculate most common mood
        val moodCounts = moods.groupingBy { it.emoji }.eachCount()
        val mostCommon = moodCounts.maxByOrNull { it.value }
        val commonEmoji = mostCommon?.key ?: "😊"
        
        // Calculate streak (consecutive days with mood entries)
        val streak = calculateMoodStreak(moods)
        
        // Log the statistics (since the UI elements are no longer in this fragment)
        Log.d("MoodStats", "Total entries: ${moods.size}")
        Log.d("MoodStats", "Most common emoji: $commonEmoji")
        Log.d("MoodStats", "Streak: $streak days")
    }
    
    private fun calculateMoodStreak(moods: List<MoodEntry>): Int {
        if (moods.isEmpty()) return 0
        
        // Sort moods by date (most recent first)
        val sortedMoods = moods.sortedByDescending { it.timestamp.time }
        
        var streak = 0
        var currentDate = Calendar.getInstance()
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)
        
        for (mood in sortedMoods) {
            val moodDate = Calendar.getInstance()
            moodDate.time = mood.timestamp
            moodDate.set(Calendar.HOUR_OF_DAY, 0)
            moodDate.set(Calendar.MINUTE, 0)
            moodDate.set(Calendar.SECOND, 0)
            moodDate.set(Calendar.MILLISECOND, 0)
            
            if (moodDate.timeInMillis == currentDate.timeInMillis) {
                streak++
                currentDate.add(Calendar.DAY_OF_MONTH, -1)
            } else if (moodDate.timeInMillis < currentDate.timeInMillis) {
                break
            }
        }
        
        return streak
    }
    
    /**
     * Setup weekly bar chart with emoji indicators, trend line, and interactive features
     * Note: Chart moved to home page, this is now a no-op function
     */
    private fun setupWeeklyBarChart() {
        // Chart moved to home page
        // This function is kept for backward compatibility but doesn't do anything now
        Log.d("MoodFragment", "setupWeeklyBarChart - Chart moved to home page")
    }
    
    // Removed DayMoodData class as it's no longer needed
    
    /**
     * Convert emoji to numeric mood value (1-5 scale)
     */
    private fun getMoodValue(emoji: String?): Float {
        return when (emoji) {
            "😠", "😔" -> 1f  // Angry/Sad
            "😐" -> 2f         // Neutral
            "🙂" -> 3f         // Slightly Happy
            "😊" -> 4f         // Happy
            "😂" -> 5f         // Very Happy/Ecstatic
            else -> 0f         // No mood entry
        }
    }
    
    /**
     * Get color for mood value with gradient
     */
    private fun getMoodColor(value: Float): Int {
        return when {
            value >= 4.5f -> Color.parseColor("#4CAF50") // Green - Very positive
            value >= 3.5f -> Color.parseColor("#8BC34A") // Light green - Positive
            value >= 2.5f -> Color.parseColor("#FFC107") // Yellow - Neutral
            value >= 1.5f -> Color.parseColor("#FF9800") // Orange - Negative
            value > 0f -> Color.parseColor("#F44336")    // Red - Very negative
            else -> Color.parseColor("#E0E0E0")          // Gray - No data
        }
    }
    
    /**
     * Generate auto insights from weekly mood pattern
     * Note: Pattern insights moved to home page, this is now a no-op function
     */
    private fun generatePatternInsights() {
        // Pattern insights moved to home page
        // This function is kept for backward compatibility but doesn't do anything now
        Log.d("MoodFragment", "generatePatternInsights - Pattern insights moved to home page")
    }
    
    // Removed showMoodDetailToast as it's no longer needed
    
    private fun setupClickListeners() {
        binding.btnAddMood.setOnClickListener {
            showAddMoodDialog()
        }
        
        // Handle empty state button click
        binding.btnAddFirstMood.setOnClickListener {
            showAddMoodDialog()
        }
    }
    
    /**
     * Setup circular day selectors with animations
     * Note: Day selectors moved to home page, this is now a no-op function
     */
    private fun setupDaySelectors() {
        // Day selectors moved to home page
        // This function is kept for backward compatibility but doesn't do anything now
        Log.d("MoodFragment", "setupDaySelectors - Day selectors moved to home page")
    }
    
    /**
     * Animate day selector on click
     * Note: Day selectors moved to home page, this is now a no-op function
     */
    private fun animateDaySelector(view: View) {
        // Day selectors moved to home page
        // This function is kept for backward compatibility but doesn't do anything now
        Log.d("MoodFragment", "animateDaySelector - Day selectors moved to home page")
    }
    
    /**
     * Animate card entrance with staggered effect
     * This method is now empty as stats container moved to home page
     */
    private fun animateCardEntrance() {
        // Stats container moved to home page
        // This function is kept for backward compatibility but doesn't do anything now
        Log.d("MoodFragment", "animateCardEntrance - Stats container moved to home page")
    }
    
    private fun showAddMoodDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(com.example.welltracker.R.layout.dialog_add_mood, null)
        
        // Setup blur effect for dialog
        setupDialogBlurEffect(dialogView)
        
        val etMoodNote = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.welltracker.R.id.et_mood_note)
        
        // Get the emoji TextViews (still needed for visual feedback)
        val emojiViews = listOf(
            dialogView.findViewById<android.widget.TextView>(com.example.welltracker.R.id.tv_emoji_sad),
            dialogView.findViewById<android.widget.TextView>(com.example.welltracker.R.id.tv_emoji_neutral),
            dialogView.findViewById<android.widget.TextView>(com.example.welltracker.R.id.tv_emoji_slightly_happy),
            dialogView.findViewById<android.widget.TextView>(com.example.welltracker.R.id.tv_emoji_happy),
            dialogView.findViewById<android.widget.TextView>(com.example.welltracker.R.id.tv_emoji_very_happy)
        )
        
        // Get the CardView containers (these will be the click targets)
        val cardViews = listOf(
            emojiViews[0].parent.parent as androidx.cardview.widget.CardView, // Sad
            emojiViews[1].parent.parent as androidx.cardview.widget.CardView, // Neutral
            emojiViews[2].parent.parent as androidx.cardview.widget.CardView, // Slightly Happy
            emojiViews[3].parent.parent as androidx.cardview.widget.CardView, // Happy
            emojiViews[4].parent.parent as androidx.cardview.widget.CardView  // Very Happy
        )
        
        val emojis = listOf("😔", "😐", "🙂", "😊", "😂")
        var selectedEmoji = "🙂"
        var selectedIndex = 2 // Default to slightly happy
        
        // Set click listeners on the CardViews for better UX
        cardViews.forEachIndexed { index, cardView ->
            cardView.setOnClickListener {
                selectedEmoji = emojis[index]
                selectedIndex = index
                
                // Update visual feedback - add elevation and change emoji background
                cardViews.forEachIndexed { i, card ->
                    if (i == index) {
                        card.cardElevation = 12f
                        card.scaleX = 1.05f
                        card.scaleY = 1.05f
                    } else {
                        card.cardElevation = 4f
                        card.scaleX = 1.0f
                        card.scaleY = 1.0f
                    }
                }
                
                // Update emoji background
                emojiViews.forEachIndexed { i, view ->
                    view.background = context?.getDrawable(
                        if (i == index) com.example.welltracker.R.drawable.bg_emoji_selected 
                        else com.example.welltracker.R.drawable.bg_emoji_unselected
                    )
                }
            }
        }
        
        // Set initial selection (slightly happy)
        cardViews[selectedIndex].cardElevation = 12f
        cardViews[selectedIndex].scaleX = 1.05f
        cardViews[selectedIndex].scaleY = 1.05f
        emojiViews[selectedIndex].background = context?.getDrawable(com.example.welltracker.R.drawable.bg_emoji_selected)
        
        dialog.setView(dialogView)
            .setPositiveButton("Save Mood") { _, _ ->
                val note = etMoodNote.text.toString().trim()
                
                // Allow saving without a note (note is optional)
                viewModel.addMood(selectedEmoji, note.ifEmpty { "No additional notes" })
                android.widget.Toast.makeText(context, "Mood saved successfully! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                // Explicitly reload moods to ensure UI updates immediately
                viewModel.loadMoods()
            }
            .setNegativeButton("Cancel", null)
        
        // Create and show the dialog with custom styling
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
        
        // Style the buttons
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(context.getColor(com.example.welltracker.R.color.primary_purple))
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        
        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(context.getColor(com.example.welltracker.R.color.text_secondary))
            textSize = 16f
        }
    }
    
    private fun setupDialogBlurEffect(dialogView: View) {
        // Temporarily disabled blur effects due to BlurView dependency issues
        // TODO: Re-enable once BlurView library is properly integrated
        Log.d("BlurSetup", "Dialog blur effects temporarily disabled")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
