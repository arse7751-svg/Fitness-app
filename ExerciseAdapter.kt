package com.arsenii.fitnessapp

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Manages the main exercise list
class ExerciseAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var exercises = listOf<Pair<String, String>>()
    private var searchQuery = ""

    // Updates the list of exercises to show
    fun updateData(newExercises: List<Pair<String, String>>, query: String) {
        this.exercises = newExercises
        this.searchQuery = query
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_list_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val (name, description) = exercises[position]
        holder.bind(name, description, searchQuery) {
            val clickedExerciseName = exercises[position].first
            onItemClick(clickedExerciseName)
        }
    }

    override fun getItemCount(): Int = exercises.size

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exerciseNameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.exerciseDescriptionTextView)

        fun bind(name: String, description: String, query: String, clickListener: () -> Unit) {
            nameTextView.text = highlightText(name, query)
            descriptionTextView.text = highlightText(description, query)
            itemView.setOnClickListener { clickListener() }
        }

        // Highlights the search query in text
        private fun highlightText(text: String, query: String): SpannableString {
            val spannable = SpannableString(text)
            if (query.isNotEmpty() && text.contains(query, ignoreCase = true)) {
                val startIndex = text.indexOf(query, ignoreCase = true)
                spannable.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    startIndex,
                    startIndex + query.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return spannable
        }
    }
}
