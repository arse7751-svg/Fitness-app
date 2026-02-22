package com.arsenii.fitnessapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReplaceExerciseActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replace_exercise)

        findViewById<ImageView>(R.id.closeButton).setOnClickListener { finish() }

        val muscleGroup = intent.getStringExtra("MUSCLE_GROUP")
        val exerciseToReplace = intent.getStringExtra("EXERCISE_TO_REPLACE")
        val currentExercises = intent.getStringArrayListExtra("CURRENT_EXERCISES")

        if (muscleGroup != null && currentExercises != null) {
            setupRecyclerView(muscleGroup, exerciseToReplace, currentExercises)
        }
    }

    // Sets up the list of replacement exercises
    private fun setupRecyclerView(muscleGroup: String, exerciseToReplace: String?, currentExercises: ArrayList<String>) {
        val recyclerView = findViewById<RecyclerView>(R.id.replacementExercisesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get all exercises for the muscle group, but remove ones already in the workout
        val possibleExercises = WorkoutData.getExercisesForMuscleGroup(muscleGroup)
        val filteredExercises = possibleExercises.filter { it != exerciseToReplace && !currentExercises.contains(it) }

        val maxWeightsPrefs = UserDataManager.getUserPrefs(this, "MaxWeights")
        val ratingPrefs = UserDataManager.getUserPrefs(this, "ExerciseRatings")

        // Create a list of replacement exercises with their stats
        val replacementList = filteredExercises.map {
            val actual1RM = maxWeightsPrefs.getFloat("${it}_1RM", 0f)
            val estimated1RM = maxWeightsPrefs.getFloat("${it}_estimated_1RM", 0f)
            val rating = ratingPrefs.getFloat("${it}_rating", 0f)
            ReplacementExercise(
                name = it,
                description = ExerciseDescriptions.descriptions[it],
                actual1RM = actual1RM,
                estimated1RM = estimated1RM,
                rating = rating
            )
        }

        // When an exercise is selected, send it back to the previous screen
        val adapter = ReplacementExerciseAdapter(this, replacementList) { selectedExercise ->
            val resultIntent = Intent().apply {
                putExtra("NEW_EXERCISE", selectedExercise)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        recyclerView.adapter = adapter
    }
}
