package com.example.welltracker.ui.home

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
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.databinding.FragmentHomeBinding
import com.example.welltracker.data.model.MoodEntry
import com.example.welltracker.viewmodel.HomeViewModel
import com.example.welltracker.viewmodel.MoodsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private val moodsViewModel: MoodsViewModel by viewModels()

    private lateinit var prefsStore: PrefsStore
    private var analysisType = 7 // Default to 7 days
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsStore = PrefsStore(requireContext())
        
        setupBlurEffects()
        setupObservers()
        setupClickListeners()
        setupDaySelectors()
        viewModel.loadData()
        moodsViewModel.loadMoods()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadData()
        moodsViewModel.loadMoods()
    }
    
    private fun setupObservers() {
        viewModel.todayProgress.observe(viewLifecycleOwner, Observer { progress ->
            val (completed, total) = progress
            binding.tvProgressCount.text = "$completed/$total"
            binding.tvProgressPercentage.text = getString(com.example.welltracker.R.string.progress_percentage, getCompletionPercentage(completed, total))

            val progressPercentage = getCompletionPercentage(completed, total)
            binding.progressBar.progress = progressPercentage
        })
        
        // Observe mood data for the weekly overview chart
        moodsViewModel.moods.observe(viewLifecycleOwner, Observer { moods ->
            updateMoodTrendsChart(moods)
        })
    }
    
    private fun setupBlurEffects() {
        // Temporarily disabled blur effects due to BlurView dependency issues
        // TODO: Re-enable once BlurView library is properly integrated
        Log.d("BlurSetup", "Blur effects temporarily disabled")
    }
    
    // Mood chart setup removed - chart now displayed in MoodFragment
    
    private fun setupClickListeners() {
        // Navigate to Habits section
        binding.cardHabits.setOnClickListener {
            navigateToFragment(com.example.welltracker.ui.habits.HabitsFragment())
        }
        
        // Navigate to Mood section
        binding.cardMood.setOnClickListener {
            navigateToFragment(com.example.welltracker.ui.mood.MoodFragment())
        }
        
        // Navigate to Hydration section
        binding.cardHydration.setOnClickListener {
            navigateToFragment(com.example.welltracker.ui.hydration.HydrationFragment())
        }
        
        // Navigate to Settings section
        binding.cardSettings.setOnClickListener {
            navigateToFragment(com.example.welltracker.ui.settings.SettingsFragment())
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
            is com.example.welltracker.ui.habits.HabitsFragment -> com.example.welltracker.R.id.nav_habits
            is com.example.welltracker.ui.mood.MoodFragment -> com.example.welltracker.R.id.nav_mood
            is com.example.welltracker.ui.hydration.HydrationFragment -> com.example.welltracker.R.id.nav_water
            is com.example.welltracker.ui.settings.SettingsFragment -> com.example.welltracker.R.id.nav_settings
            else -> com.example.welltracker.R.id.nav_home
        }
    }
    
    private fun getCompletionPercentage(completed: Int, total: Int): Int {
        return if (total > 0) {
            (completed * 100) / total
        } else {
            0
        }
    }
    
    /**
     * Setup circular day selectors with animations
     */
    private fun setupDaySelectors() {
        val dayContainers = listOf(
            binding.dayMondayHome,
            binding.dayTuesdayHome,
            binding.dayWednesdayHome,
            binding.dayThursdayHome,
            binding.dayFridayHome,
            binding.daySaturdayHome,
            binding.daySundayHome
        )
        
        dayContainers.forEachIndexed { index, dayContainer ->
            // Add click animation
            dayContainer.setOnClickListener {
                animateDaySelector(dayContainer)
                
                // Show mood info for that day
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -(6 - index))
                val dayFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
                
                Toast.makeText(
                    context,
                    "Viewing mood for ${dayFormat.format(calendar.time)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            // Add entrance animation with delay
            dayContainer.alpha = 0f
            dayContainer.translationY = 50f
            dayContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(50L * index)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }
    
    /**
     * Animate day selector on click
     */
    private fun animateDaySelector(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(100)
                    .withEndAction {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
            .start()
    }
    
    /**
     * Update Mood Trends weekly overview chart on homepage
     */
    private fun updateMoodTrendsChart(moods: List<MoodEntry>) {
        if (moods.isEmpty()) {
            binding.chartPlaceholderHome.visibility = View.VISIBLE
            binding.moodBarChartHome.visibility = View.GONE
            binding.tvPatternInsightHome.visibility = View.GONE
            return
        }

        // Show chart and hide placeholder
        binding.chartPlaceholderHome.visibility = View.GONE
        binding.moodBarChartHome.visibility = View.VISIBLE

        // Prepare data for last 7 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayLabels = mutableListOf<String>()
        val barEntries = mutableListOf<BarEntry>()
        val lineEntries = mutableListOf<Entry>()
        val colors = mutableListOf<Int>()

        val weekData = mutableListOf<DayMoodData>()

        // Collect mood data for last 7 days
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val dayMood = moods.find { mood ->
                val moodCal = Calendar.getInstance().apply { time = mood.timestamp }
                moodCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                moodCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
            }

            val moodValue = getMoodValue(dayMood?.emoji)
            val dayLabel = dateFormat.format(calendar.time)

            dayLabels.add(dayLabel)
            barEntries.add(BarEntry((6 - i).toFloat(), moodValue))
            lineEntries.add(Entry((6 - i).toFloat(), moodValue))
            colors.add(getMoodColor(moodValue))

            weekData.add(DayMoodData(dayLabel, dayMood?.emoji, moodValue))
        }

        // Create bar dataset
        val barDataSet = BarDataSet(barEntries, "Daily Mood").apply {
            setColors(colors)
            setDrawValues(false)
            highLightAlpha = 200
        }

        // Create line dataset for trend
        val lineDataSet = LineDataSet(lineEntries, "Trend").apply {
            color = Color.parseColor("#6B46C1") // primary_purple
            lineWidth = 2.5f
            setDrawCircles(true)
            circleRadius = 4f
            setCircleColor(Color.parseColor("#6B46C1"))
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(false)
        }

        // Combine data
        val combinedData = CombinedData().apply {
            setData(BarData(barDataSet))
            setData(LineData(lineDataSet))
        }

        // Configure chart
        binding.moodBarChartHome.apply {
            data = combinedData
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawBorders(false)

            // X-Axis configuration
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 7
                valueFormatter = IndexAxisValueFormatter(dayLabels)
                textColor = Color.parseColor("#6B7280") // text_secondary
                textSize = 10f
            }
            
            // Left Y-Axis configuration
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 5.5f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB") // divider
                gridLineWidth = 0.5f
                textColor = Color.parseColor("#6B7280")
                textSize = 9f
                setDrawLabels(false) // Hide y-axis labels for cleaner look
            }
            
            // Right Y-Axis (disabled)
            axisRight.isEnabled = false
            
            // Animate chart
            animateY(1000, Easing.EaseOutCubic)

            // Set interactive listener
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val index = e.x.toInt()
                        if (index in weekData.indices) {
                            val dayData = weekData[index]
                            showMoodDetailToast(dayData)
                        }
                    }
                }

                override fun onNothingSelected() {
                    // Do nothing
                }
            })

            invalidate()
        }

        // Update mood statistics
        updateMoodStatisticsHome(moods, barEntries)

        // Generate and display pattern insights
        generatePatternInsightsHome(weekData, moods)
    }

    /**
     * Data class for day mood information
     */
    private data class DayMoodData(
        val dayLabel: String,
        val emoji: String?,
        val value: Float
    )

    /**
     * Convert emoji to numeric mood value (1-5 scale)
     */
    private fun getMoodValue(emoji: String?): Float {
        return when (emoji) {
            "😠", "😔" -> 1f  // Angry/Sad
            "😐" -> 2f         // Neutral
            "🙂" -> 3f         // Slightly Happy
            "😊" -> 4f         // Happy
            "😂", "🥳" -> 5f   // Very Happy/Ecstatic
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
     * Update mood statistics in the home chart
     */
    private fun updateMoodStatisticsHome(moods: List<MoodEntry>, entries: List<BarEntry>) {
        if (moods.isEmpty()) {
            binding.bestMoodBadgeHome.visibility = View.GONE
            return
        }

        // Calculate most common mood
        val moodCounts = moods.groupingBy { it.emoji }.eachCount()
        val mostCommon = moodCounts.maxByOrNull { it.value }
        binding.tvMostCommonEmojiHome.text = mostCommon?.key ?: "😊"

        // Update total entries
        binding.tvTotalEntriesHome.text = moods.size.toString()

        // Calculate streak (consecutive days with mood entries)
        val streak = calculateMoodStreak(moods)
        binding.tvMoodStreakHome.text = "🔥 $streak"

        // Update best mood day badge
        val validEntries = entries.filter { it.y > 0f }
        if (validEntries.isNotEmpty()) {
            val bestDayIndex = validEntries.maxByOrNull { it.y }?.x?.toInt() ?: 0
            val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val bestDay = dayNames.getOrElse(bestDayIndex) { "Today" }
            binding.tvBestDayHome.text = bestDay
            binding.bestMoodBadgeHome.visibility = View.VISIBLE
        } else {
            binding.bestMoodBadgeHome.visibility = View.GONE
        }
    }

    /**
     * Calculate mood streak (consecutive days with entries)
     */
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
     * Generate pattern insights from weekly mood data
     */
    private fun generatePatternInsightsHome(weekData: List<DayMoodData>, allMoods: List<MoodEntry>) {
        val validDays = weekData.filter { it.value > 0f }

        if (validDays.isEmpty()) {
            binding.tvPatternInsightHome.visibility = View.GONE
            return
        }
        
        binding.tvPatternInsightHome.visibility = View.VISIBLE

        // Find best mood day
        val bestDay = validDays.maxByOrNull { it.value }

        // Calculate weekly average
        val weeklyAverage = validDays.map { it.value }.average()

        // Calculate previous week average (if available)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -14)
        val twoWeeksAgo = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val oneWeekAgo = calendar.time

        val previousWeekMoods = allMoods.filter { mood ->
            mood.timestamp.after(twoWeeksAgo) && mood.timestamp.before(oneWeekAgo)
        }
        
        val insightText = buildString {
            append("💡 ")

            // Best day insight
            bestDay?.let {
                append("Best mood on ${it.dayLabel}")
            }

            // Percentage change from last week
            if (previousWeekMoods.isNotEmpty()) {
                val previousAverage = previousWeekMoods.map { getMoodValue(it.emoji) }.average()
                val percentChange = ((weeklyAverage - previousAverage) / previousAverage * 100).toInt()

                when {
                    percentChange > 5 -> append(" • ${percentChange}% improvement")
                    percentChange < -5 -> append(" • ${Math.abs(percentChange)}% decrease")
                    else -> append(" • Stable mood")
                }
            } else {
                // First week insight
                when {
                    weeklyAverage >= 4.0 -> append(" • Amazing week!")
                    weeklyAverage >= 3.0 -> append(" • Good week")
                    else -> append(" • Take care")
                }
            }
        }
        
        binding.tvPatternInsightHome.text = insightText
    }
    
    /**
     * Show mood detail when bar is tapped
     */
    private fun showMoodDetailToast(dayData: DayMoodData) {
        if (dayData.emoji == null) {
            Toast.makeText(context, "No mood entry for ${dayData.dayLabel}", Toast.LENGTH_SHORT).show()
        } else {
            val moodName = when (dayData.emoji) {
                "😠" -> "Angry"
                "😔" -> "Sad"
                "😐" -> "Neutral"
                "🙂" -> "Slightly Happy"
                "😊" -> "Happy"
                "😂", "🥳" -> "Ecstatic"
                else -> "Unknown"
            }
            Toast.makeText(
                context,
                "${dayData.dayLabel}: $moodName ${dayData.emoji}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
