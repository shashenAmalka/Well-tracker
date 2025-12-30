package com.example.welltracker.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.welltracker.data.model.MoodEntry
import com.example.welltracker.databinding.ItemMoodBinding
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(
    private val onDelete: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {
    
    private var moods = listOf<MoodEntry>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
    
    fun updateMoods(newMoods: List<MoodEntry>) {
        moods = newMoods
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }
    
    override fun getItemCount(): Int = moods.size
    
    inner class MoodViewHolder(private val binding: ItemMoodBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mood: MoodEntry) {
            binding.tvMoodEmoji.text = mood.emoji
            binding.tvMoodNote.text = mood.note
            binding.tvMoodDate.text = dateFormat.format(mood.timestamp)
            
            binding.ivDeleteMood.setOnClickListener {
                onDelete(mood)
            }
        }
    }
}
