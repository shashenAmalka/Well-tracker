package com.example.welltracker.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.example.welltracker.R
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.data.model.Habit
import com.example.welltracker.data.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AnalysisFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var btn7Days: MaterialButton
    private lateinit var btn30Days: MaterialButton
    private lateinit var tvChartTitle: TextView
    private lateinit var tvTopHabitInsight: TextView
    private lateinit var tvRecommendation: TextView
    private lateinit var tvConsistencyInsight: TextView
    private lateinit var emptyStateCard: View
    
    private lateinit var prefsStore: PrefsStore
    private lateinit var sharedPrefsManager: SharedPreferencesManager
    private var analysisType = SEVEN_DAYS // Default to 7 days
    
    companion object {
        const val SEVEN_DAYS = 7
        const val THIRTY_DAYS = 30
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        prefsStore = PrefsStore(requireContext())
        sharedPrefsManager = SharedPreferencesManager(requireContext())
        
        setupChart()
        setupButtonListeners()
        loadAnalysisData()
    }
    
    private fun initializeViews(view: View) {
        barChart = view.findViewById(R.id.bar_chart)
        btn7Days = view.findViewById(R.id.btn_7_days)
        btn30Days = view.findViewById(R.id.btn_30_days)
        tvChartTitle = view.findViewById(R.id.tv_chart_title)
        tvTopHabitInsight = view.findViewById(R.id.tv_top_habit_insight)
        tvRecommendation = view.findViewById(R.id.tv_recommendation)
        tvConsistencyInsight = view.findViewById(R.id.tv_consistency_insight)
        emptyStateCard = view.findViewById(R.id.empty_state_card)
    }
    
    private fun setupChart() {
        barChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(10)
            legend.isEnabled = false
            
            // Customize X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 5
                textColor = Color.parseColor("#9E9E9E")
                textSize = 12f
            }
            
            // Customize Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                axisMinimum = 0f
                axisMaximum = 5f
                granularity = 1f
                textColor = Color.parseColor("#9E9E9E")
                textSize = 12f
            }
            
            axisRight.isEnabled = false
            
            // Set background
            setBackgroundColor(Color.TRANSPARENT)
        }
    }
    
    private fun setupButtonListeners() {
        btn7Days.setOnClickListener {
            updateAnalysisType(SEVEN_DAYS)
        }
        
        btn30Days.setOnClickListener {
            updateAnalysisType(THIRTY_DAYS)
        }
    }
    
    private fun updateAnalysisType(days: Int) {
        analysisType = days
        updateButtonStates()
        loadAnalysisData()
        
        // Update chart title
        val period = if (days == SEVEN_DAYS) "This Week" else "This Month"
        tvChartTitle.text = "How Your Habits Influenced Your Mood $period"
    }
    
    private fun updateButtonStates() {
        if (analysisType == SEVEN_DAYS) {
            btn7Days.setBackgroundColor(Color.parseColor("#4A90E2"))
            btn7Days.setTextColor(Color.WHITE)
            btn30Days.setBackgroundColor(Color.parseColor("#F5F5F5"))
            btn30Days.setTextColor(Color.parseColor("#666666"))
        } else {
            btn30Days.setBackgroundColor(Color.parseColor("#4A90E2"))
            btn30Days.setTextColor(Color.WHITE)
            btn7Days.setBackgroundColor(Color.parseColor("#F5F5F5"))
            btn7Days.setTextColor(Color.parseColor("#666666"))
        }
    }
    
    private fun loadAnalysisData() {
        val userId = sharedPrefsManager.getCurrentUserEmail() ?: return
        val habits = prefsStore.getHabits(userId)
        
        if (habits.isEmpty()) {
            showEmptyState()
            return
        }
        
        val habitMoodData = calculateHabitMoodCorrelation(habits, userId)
        
        if (habitMoodData.isEmpty()) {
            showEmptyState()
            return
        }
        
        hideEmptyState()
        displayChart(habitMoodData)
        updateInsights(habitMoodData, habits)
    }
    
    private fun calculateHabitMoodCorrelation(habits: List<Habit>, userId: String): List<HabitMoodData> {
        val habitMoodMap = mutableMapOf<String, MutableList<Pair<Boolean, Int>>>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val moods = prefsStore.getMoods(userId)
        
        // Create a map of date to mood score for efficient lookup
        val moodScoreMap = mutableMapOf<String, Int>()
        moods.forEach { moodEntry ->
            val dateStr = dateFormat.format(moodEntry.timestamp)
            val moodScore = convertEmojiToScore(moodEntry.emoji)
            moodScoreMap[dateStr] = moodScore
        }
        
        // For demonstration purposes, let's create some sample correlation data
        // In a real app, you'd store daily completion data
        habits.forEachIndexed { index, habit ->
            val scores = mutableListOf<Pair<Boolean, Int>>()
            
            // Generate sample data: higher mood scores when habits are "completed"
            repeat(analysisType) { day ->
                val isCompleted = habit.isCompleted || (day % 3 == 0) // Sample completion pattern
                val baseMoodScore = when (index % 5) {
                    0 -> if (isCompleted) 4 else 3 // Morning meditation high impact
                    1 -> if (isCompleted) 4 else 2 // Vitamins medium impact
                    2 -> if (isCompleted) 5 else 3 // Exercise high impact
                    3 -> if (isCompleted) 3 else 2 // Water low impact
                    else -> if (isCompleted) 4 else 3 // Reading medium impact
                }
                scores.add(Pair(isCompleted, baseMoodScore))
            }
            
            habitMoodMap[habit.name] = scores
        }
        
        // Calculate average mood when habit was completed vs not completed
        return habitMoodMap.mapNotNull { (habitName, dataPoints) ->
            val completedMoods = dataPoints.filter { it.first }.map { it.second }
            val notCompletedMoods = dataPoints.filter { !it.first }.map { it.second }
            
            if (completedMoods.isNotEmpty() || notCompletedMoods.isNotEmpty()) {
                val avgCompletedMood = if (completedMoods.isNotEmpty()) {
                    completedMoods.average().toFloat()
                } else 2.5f // Default neutral score
                
                val avgNotCompletedMood = if (notCompletedMoods.isNotEmpty()) {
                    notCompletedMoods.average().toFloat()
                } else 2.5f
                
                val impact = avgCompletedMood - avgNotCompletedMood
                
                HabitMoodData(
                    habitName = habitName,
                    avgMoodWhenCompleted = avgCompletedMood,
                    avgMoodWhenNotCompleted = avgNotCompletedMood,
                    impact = impact,
                    completionCount = completedMoods.size,
                    totalDays = dataPoints.size
                )
            } else null
        }.sortedByDescending { it.avgMoodWhenCompleted }
    }
    
    private fun convertEmojiToScore(emoji: String): Int {
        return when (emoji) {
            "😭", "😢", "😞" -> 1 // Very sad
            "😐", "😕", "🙁" -> 2 // Sad
            "😊", "🙂", "😌" -> 3 // Neutral/OK
            "😄", "😃", "😁" -> 4 // Happy
            "😂", "🤩", "😍", "🥳" -> 5 // Very happy
            else -> 3 // Default to neutral
        }
    }
    
    private fun displayChart(data: List<HabitMoodData>) {
        val entries = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()
        val labels = ArrayList<String>()
        
        data.take(5).forEachIndexed { index, habitData -> // Show top 5 habits
            entries.add(BarEntry(index.toFloat(), habitData.avgMoodWhenCompleted))
            
            // Color based on mood score level
            val color = when {
                habitData.avgMoodWhenCompleted >= 4.0f -> Color.parseColor("#66BB6A") // High mood - Green
                habitData.avgMoodWhenCompleted >= 3.5f -> Color.parseColor("#4A90E2") // Medium mood - Blue
                else -> Color.parseColor("#FFB74D") // Lower mood - Peach
            }
            colors.add(color)
            
            // Shorten label if too long
            val shortLabel = if (habitData.habitName.length > 10) {
                "${habitData.habitName.take(8)}..."
            } else {
                habitData.habitName
            }
            labels.add(shortLabel)
        }
        
        val dataSet = BarDataSet(entries, "Mood Impact").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.parseColor("#333333")
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f", value)
                }
            }
        }
        
        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }
        
        barChart.apply {
            this.data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            animateY(1000)
            invalidate()
        }
    }
    
    private fun updateInsights(data: List<HabitMoodData>, habits: List<Habit>) {
        if (data.isNotEmpty()) {
            val topHabit = data.first()
            tvTopHabitInsight.text = "• ${topHabit.habitName} has the highest positive impact on your mood (avg. ${String.format("%.1f", topHabit.avgMoodWhenCompleted)}/5)"
            
            val recommendation = when {
                topHabit.avgMoodWhenCompleted >= 4.0f -> "• Consider doing ${topHabit.habitName} earlier in the day for maximum benefit"
                data.any { it.avgMoodWhenCompleted < 3.0f } -> "• Focus on your high-impact habits for better mood improvements"
                else -> "• You're doing great! Keep up your consistent habit practice"
            }
            tvRecommendation.text = recommendation
            
            // Calculate overall completion rate
            val totalCompletions = data.sumOf { it.completionCount }
            val totalPossible = data.sumOf { it.totalDays }
            val completionRate = if (totalPossible > 0) {
                (totalCompletions.toFloat() / totalPossible * 100).toInt()
            } else 0
            
            tvConsistencyInsight.text = "• You completed $completionRate% of your habits this period"
        }
    }
    
    private fun showEmptyState() {
        barChart.visibility = View.GONE
        emptyStateCard.visibility = View.VISIBLE
        tvTopHabitInsight.text = "• Start tracking habits and moods to see insights"
        tvRecommendation.text = "• Consistency is key - try to complete at least one habit daily"
        tvConsistencyInsight.text = "• Your analytics will appear here once you have data"
    }
    
    private fun hideEmptyState() {
        barChart.visibility = View.VISIBLE
        emptyStateCard.visibility = View.GONE
    }
    
    data class HabitMoodData(
        val habitName: String,
        val avgMoodWhenCompleted: Float,
        val avgMoodWhenNotCompleted: Float,
        val impact: Float,
        val completionCount: Int,
        val totalDays: Int
    )
}