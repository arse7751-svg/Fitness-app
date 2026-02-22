package com.arsenii.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MaxWeightsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_max_weights)

        findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            finish()
        }

        calculateAndDisplayMaxWeights()
    }

    // Reads history, finds best lift for each exercise
    private fun calculateAndDisplayMaxWeights() {
        val maxWeightContainer = findViewById<LinearLayout>(R.id.maxWeightContainer)
        val historyPrefs = UserDataManager.getUserPrefs(this, "WorkoutHistory")
        val maxWeights = mutableMapOf<String, Pair<Float, String>>()

        val gson = Gson()
        val type = object : TypeToken<Map<String, List<Map<String, String>>>>() {}.type

        // Loop all saved workouts
        for ((date, value) in historyPrefs.all) {
            if (value is String) {
                // Converts saved data back to workout object
                val workoutLogs: Map<String, List<Map<String, String>>> = gson.fromJson(value, type)
                
                for ((exercise, sets) in workoutLogs) {
                    for (set in sets) {
                        val weight = set["weight"]?.toFloatOrNull() ?: 0f
                        
                        // If weight is a new record, save it
                        if (weight > (maxWeights[exercise]?.first ?: 0f)) {
                            maxWeights[exercise] = Pair(weight, date)
                        }
                    }
                }
            }
        }

        val allExercises = ExerciseDescriptions.descriptions.keys.sorted()

        // Make a view for each exercise
        for (exerciseName in allExercises) {
            val maxWeightPair = maxWeights[exerciseName]
            val maxWeight = maxWeightPair?.first?.toString() ?: "--"
            val date = maxWeightPair?.second

            val maxWeightView = LayoutInflater.from(this).inflate(R.layout.max_weight_item, maxWeightContainer, false)
            val exerciseNameTextView = maxWeightView.findViewById<TextView>(R.id.exerciseNameTextView)
            val maxWeightTextView = maxWeightView.findViewById<TextView>(R.id.maxWeightTextView)

            exerciseNameTextView.text = exerciseName
            maxWeightTextView.text = "$maxWeight kg"

            // Only clickable if record exists
            if (date != null) {
                maxWeightView.setOnClickListener { view ->
                    view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).withEndAction {
                        // On click, go to history for that day
                        val intent = Intent(this, HistoryActivity::class.java).apply {
                            putExtra("SELECTED_DATE", date)
                        }
                        startActivity(intent)
                        view.scaleX = 1f
                        view.scaleY = 1f
                    }.start()
                }
            }
            maxWeightContainer.addView(maxWeightView)
        }
    }
}
