package com.arsenii.fitnessapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

// Adapter for list with checkboxes
class CheckableExerciseAdapter(
    private val allExercises: List<String>,
    private val onExerciseChecked: (String, Boolean) -> Unit
) : RecyclerView.Adapter<CheckableExerciseAdapter.ViewHolder>() {

    private var displayedExercises = allExercises.toMutableList()
    private var selectedExercises = setOf<String>()

    // Updates which exercises are checked
    fun setSelected(selected: Set<String>) {
        this.selectedExercises = selected
        notifyDataSetChanged()
    }

    // Filters list based on search
    fun filter(query: String) {
        displayedExercises = if (query.isEmpty()) {
            allExercises.toMutableList()
        } else {
            allExercises.filter { it.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.checkable_exercise_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = displayedExercises[position]
        holder.bind(exercise, selectedExercises.contains(exercise)) { isChecked ->
            onExerciseChecked(exercise, isChecked)
        }
    }

    override fun getItemCount(): Int = displayedExercises.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.exerciseCheckBox)

        fun bind(exerciseName: String, isChecked: Boolean, onChecked: (Boolean) -> Unit) {
            // Set listener to null before changing checked state
            checkBox.setOnCheckedChangeListener(null)
            checkBox.text = exerciseName
            checkBox.isChecked = isChecked
            checkBox.setOnCheckedChangeListener { _, newCheckedState ->
                onChecked(newCheckedState)
            }
        }
    }
}
