package com.arsenii.fitnessapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.round

// Holds all info for a replacement exercise item
data class ReplacementExercise(
    val name: String,
    val description: String?,
    val actual1RM: Float,
    val estimated1RM: Float,
    val rating: Float
)

class ReplacementExerciseAdapter(
    private val context: Context,
    private val exercises: List<ReplacementExercise>,
    private val onExerciseSelected: (String) -> Unit
) : RecyclerView.Adapter<ReplacementExerciseAdapter.ViewHolder>() {

    private var expandedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clickableHeader: View = view.findViewById(R.id.clickableHeader)
        val exerciseNameTextView: TextView = view.findViewById(R.id.exerciseNameTextView)
        val arrowIcon: ImageView = view.findViewById(R.id.arrowIcon)
        val expandableContent: LinearLayout = view.findViewById(R.id.expandableContent)
        val exerciseImageView: ImageView = view.findViewById(R.id.exerciseImageView)
        val exerciseDescriptionTextView: TextView = view.findViewById(R.id.exerciseDescriptionTextView)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val oneRepMaxTextView: TextView = view.findViewById(R.id.oneRepMaxTextView)
        val swapButton: Button = view.findViewById(R.id.swapButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rich_replacement_exercise_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        val isExpanded = position == expandedPosition

        holder.exerciseNameTextView.text = exercise.name
        holder.expandableContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.arrowIcon.rotation = if (isExpanded) 180f else 0f

        holder.clickableHeader.setOnClickListener {
            val oldExpandedPosition = expandedPosition
            expandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(oldExpandedPosition)
            notifyItemChanged(expandedPosition)
        }

        if (isExpanded) {
            holder.exerciseDescriptionTextView.text = exercise.description

            val imageName = getDrawableNameFor(exercise.name)
            val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (imageResId != 0) {
                holder.exerciseImageView.setImageResource(imageResId)
            } else {
                holder.exerciseImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            val bestOneRepMax = maxOf(exercise.actual1RM, exercise.estimated1RM)
            if (bestOneRepMax > 0f) {
                val rounded1RM = round(bestOneRepMax * 10) / 10f
                val label = if (exercise.actual1RM >= exercise.estimated1RM) "1RM Record:" else "Est. 1RM:"
                holder.oneRepMaxTextView.text = "$label $rounded1RM kg"
                holder.oneRepMaxTextView.visibility = View.VISIBLE
            } else {
                holder.oneRepMaxTextView.visibility = View.GONE
            }

            holder.ratingBar.rating = exercise.rating

            holder.swapButton.setOnClickListener {
                onExerciseSelected(exercise.name)
            }
        }
    }

    override fun getItemCount() = exercises.size

    private fun getDrawableNameFor(exerciseName: String): String {
        if (exerciseName == "Reverse Pec-Deck") {
            return "reverse_peck_deck"
        }
        return exerciseName.lowercase().trim()
            .replace(Regex("[^a-z0-9]+"), "_")
            .removeSuffix("_")
            .removePrefix("_")
    }
}
