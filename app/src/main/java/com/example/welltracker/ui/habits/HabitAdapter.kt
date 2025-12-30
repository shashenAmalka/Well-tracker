package com.example.welltracker.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.welltracker.data.model.Habit
import com.example.welltracker.databinding.ItemHabitBinding

class HabitAdapter(
    private val onToggleComplete: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {
    
    private var habits = listOf<Habit>()
    
    fun updateHabits(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }
    
    fun getCompletedCount(): Int {
        return habits.count { it.isCompleted }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }
    
    override fun getItemCount(): Int = habits.size
    
    inner class HabitViewHolder(private val binding: ItemHabitBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(habit: Habit) {
            binding.tvHabitName.text = habit.name
            binding.tvHabitGoal.text = habit.goal
            
            // Update completion status with improved color contrast
            if (habit.isCompleted) {
                // Show green checkmark icon
                binding.ivComplete.setImageResource(com.example.welltracker.R.drawable.ic_check_circle)
                binding.cvCompleteStatus.setCardBackgroundColor(
                    binding.root.context.getColor(com.example.welltracker.R.color.secondary_green)
                )
                binding.ivComplete.setColorFilter(
                    binding.root.context.getColor(com.example.welltracker.R.color.text_white)
                )
                
                // Apply strikethrough to habit name
                binding.tvHabitName.paintFlags = binding.tvHabitName.paintFlags or 
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                
                // Show green accent border
                binding.accentBorder.visibility = android.view.View.VISIBLE
                
                // Keep background white for readability
                binding.root.setCardBackgroundColor(
                    binding.root.context.getColor(com.example.welltracker.R.color.surface_card)
                )
            } else {
                // Show incomplete state
                binding.ivComplete.setImageResource(com.example.welltracker.R.drawable.ic_check_circle)
                binding.cvCompleteStatus.setCardBackgroundColor(
                    binding.root.context.getColor(com.example.welltracker.R.color.secondary_green_light)
                )
                binding.ivComplete.setColorFilter(
                    binding.root.context.getColor(com.example.welltracker.R.color.secondary_green)
                )
                
                // Remove strikethrough
                binding.tvHabitName.paintFlags = binding.tvHabitName.paintFlags and 
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                
                // Hide accent border
                binding.accentBorder.visibility = android.view.View.GONE
                
                // Keep background white
                binding.root.setCardBackgroundColor(
                    binding.root.context.getColor(com.example.welltracker.R.color.surface_card)
                )
            }
            
            // Set click listeners
            binding.cvCompleteStatus.setOnClickListener {
                onToggleComplete(habit)
            }
            
            binding.cvDelete.setOnClickListener {
                onDelete(habit)
            }
        }
    }
}
