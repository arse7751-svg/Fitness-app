package com.arsenii.fitnessapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutLoggerActivity : BaseActivity() {

    private var initialWorkoutState: String? = null
    private lateinit var exercises: ArrayList<String>
    private var exerciseToReplace: String? = null
    private var exerciseToReplaceIndex: Int = -1
    private var selectedExerciseView: View? = null

    private val replaceExerciseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val newExercise = it.data?.getStringExtra("NEW_EXERCISE")
            if (newExercise != null && exerciseToReplace != null) {
                updateAndSaveExercise(newExercise)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_logger)

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val saveWorkoutButton = findViewById<Button>(R.id.saveWorkoutButton)

        closeButton.setOnClickListener {
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog()
            } else {
                finish()
            }
        }

        exercises = intent.getStringArrayListExtra("EXERCISES") ?: arrayListOf()

        saveWorkoutButton.setOnClickListener {
            saveWorkoutData(true)
        }
    }

    override fun onResume() {
        super.onResume()
        val exercisesLogContainer = findViewById<LinearLayout>(R.id.exercisesLogContainer)
        if (exercises.isNotEmpty()) {
            displayExercises(exercisesLogContainer, loadWorkoutData())
        }
        initialWorkoutState = getCurrentWorkoutStateAsJson()
    }

    private fun displayExercises(container: LinearLayout, savedData: Map<String, List<Map<String, String>>>) {
        container.removeAllViews()
        selectedExerciseView = null // Reset selection

        for ((index, exerciseName) in exercises.withIndex()) {
            val exerciseView = LayoutInflater.from(this).inflate(R.layout.exercise_log_item, container, false)
            val exerciseNameTextView = exerciseView.findViewById<TextView>(R.id.exerciseNameTextView)
            val setsContainer = exerciseView.findViewById<LinearLayout>(R.id.setsContainer)
            val addSetButton = exerciseView.findViewById<Button>(R.id.addSetButton)
            val swapButton = exerciseView.findViewById<ImageView>(R.id.swapExerciseButton)

            exerciseNameTextView.text = exerciseName

            exerciseView.setOnClickListener { clickedView ->
                val isAlreadySelected = selectedExerciseView == clickedView
                if (isAlreadySelected) {
                    // Second click: navigate to details
                    if (hasUnsavedChanges()) {
                        saveWorkoutData(false) // Save progress before leaving
                    }
                    val intent = Intent(this, ExerciseDetailActivity::class.java).apply {
                        putExtra("EXERCISE_TITLE", exerciseName)
                    }
                    startActivity(intent)
                } else {
                    // First click: expand new view, collapse old one
                    selectedExerciseView?.let { animateView(it, expand = false) }
                    animateView(clickedView, expand = true)
                    selectedExerciseView = clickedView
                }
            }

            swapButton.setOnClickListener {
                if(hasUnsavedChanges()) {
                    saveWorkoutData(false)
                }
                exerciseToReplace = exerciseName
                exerciseToReplaceIndex = index
                val muscleGroup = WorkoutData.getMuscleGroupForExercise(exerciseName)
                val intent = Intent(this, ReplaceExerciseActivity::class.java).apply {
                    putExtra("MUSCLE_GROUP", muscleGroup)
                    putExtra("EXERCISE_TO_REPLACE", exerciseName)
                    putStringArrayListExtra("CURRENT_EXERCISES", exercises)
                }
                replaceExerciseLauncher.launch(intent)
            }

            addSetButton.setOnClickListener {
                addSetView(setsContainer)
            }

            val savedSets = savedData[exerciseName]
            if (savedSets != null && savedSets.isNotEmpty()) {
                for (set in savedSets) {
                    addSetView(setsContainer, set["weight"], set["reps"])
                }
            } else {
                addSetView(setsContainer) // Add an initial empty set
            }
            container.addView(exerciseView)
        }
    }

    private fun animateView(view: View, expand: Boolean) {
        val scale = if (expand) 1.05f else 1.0f
        view.animate().scaleX(scale).scaleY(scale).setDuration(200).start()
    }

    private fun updateAndSaveExercise(newExercise: String) {
        val oldExercise = exerciseToReplace ?: return

        if (exerciseToReplaceIndex != -1) {
            exercises[exerciseToReplaceIndex] = newExercise
        }
        displayExercises(findViewById(R.id.exercisesLogContainer), loadWorkoutData())

        val activePlanPrefs = UserDataManager.getUserPrefs(this, "ActiveWorkoutPlan")
        val activePlanName = activePlanPrefs.getString("activePlan", null) ?: return

        val gson = Gson()

        // Update generated workouts
        val generatedWorkoutPrefs = UserDataManager.getUserPrefs(this, "GeneratedWorkouts")
        val workoutJson = generatedWorkoutPrefs.getString("${activePlanName}_workout", null)
        if (workoutJson != null) {
            val type = object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type
            val generatedWorkout: MutableMap<String, MutableList<String>> = gson.fromJson(workoutJson, type)
            for (exerciseList in generatedWorkout.values) {
                val index = exerciseList.indexOf(oldExercise)
                if (index != -1) {
                    exerciseList[index] = newExercise
                    val newWorkoutJson = gson.toJson(generatedWorkout)
                    generatedWorkoutPrefs.edit().putString("${activePlanName}_workout", newWorkoutJson).apply()
                    break
                }
            }
        } else {
            // Update custom workouts
            val customWorkoutPrefs = UserDataManager.getUserPrefs(this, "CustomWorkouts")
            val customPlanJson = customWorkoutPrefs.getString(activePlanName, null)
            if (customPlanJson != null) {
                val type = object : TypeToken<MutableMap<String, MutableSet<String>>>() {}.type
                val customPlan: MutableMap<String, MutableSet<String>> = gson.fromJson(customPlanJson, type)
                for (exerciseSet in customPlan.values) {
                    if (exerciseSet.contains(oldExercise)) {
                        exerciseSet.remove(oldExercise)
                        exerciseSet.add(newExercise)
                        val newCustomPlanJson = gson.toJson(customPlan)
                        customWorkoutPrefs.edit().putString(activePlanName, newCustomPlanJson).apply()
                    }
                }
            }
        }
    }

    private fun addSetView(setsContainer: LinearLayout, weight: String? = null, reps: String? = null) {
        val setView = LayoutInflater.from(this).inflate(R.layout.set_log_item, setsContainer, false)
        val weightEditText = setView.findViewById<EditText>(R.id.weightEditText)
        val repsEditText = setView.findViewById<EditText>(R.id.repsEditText)
        val deleteSetButton = setView.findViewById<ImageView>(R.id.deleteSetButton)

        weight?.let { weightEditText.setText(it) }
        reps?.let { repsEditText.setText(it) }

        deleteSetButton.setOnClickListener {
            setsContainer.removeView(setView)
        }
        setsContainer.addView(setView)
    }

    private fun hasUnsavedChanges(): Boolean {
        return try {
            getCurrentWorkoutStateAsJson() != initialWorkoutState
        } catch (e: Exception) {
            Log.e("WorkoutLoggerActivity", "Error checking for unsaved changes", e)
            false
        }
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Unsaved Changes")
            .setMessage("Are you sure you want to leave without saving changes?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun getCurrentWorkoutStateAsJson(): String {
        val workoutLogs = mutableMapOf<String, MutableList<Map<String, String>>>()
        val exercisesLogContainer = findViewById<LinearLayout>(R.id.exercisesLogContainer)

        for (i in 0 until exercisesLogContainer.childCount) {
            try {
                val exerciseView = exercisesLogContainer.getChildAt(i)
                val exerciseNameTextView = exerciseView.findViewById<TextView>(R.id.exerciseNameTextView)
                val exerciseName = exerciseNameTextView.text.toString()
                val setsContainer = exerciseView.findViewById<LinearLayout>(R.id.setsContainer)
                val setsData = mutableListOf<Map<String, String>>()

                for (j in 0 until setsContainer.childCount) {
                    val setView = setsContainer.getChildAt(j)
                    val weightEditText = setView.findViewById<EditText>(R.id.weightEditText)
                    val repsEditText = setView.findViewById<EditText>(R.id.repsEditText)
                    val weight = weightEditText.text.toString()
                    val reps = repsEditText.text.toString()

                    if (weight.isNotEmpty() || reps.isNotEmpty()) {
                        setsData.add(mapOf("weight" to weight, "reps" to reps))
                    }
                }
                if (setsData.isNotEmpty()) {
                    workoutLogs[exerciseName] = setsData
                }
            } catch (e: Exception) {
                Log.e("WorkoutLoggerActivity", "Error processing exercise view at index $i. Skipping.", e)
            }
        }
        return Gson().toJson(workoutLogs)
    }

    private fun saveWorkoutData(andFinish: Boolean) {
        try {
            val currentJsonState = getCurrentWorkoutStateAsJson()
            val prefs = UserDataManager.getUserPrefs(this, "WorkoutHistory")
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            prefs.edit().putString(today, currentJsonState).apply()
            initialWorkoutState = currentJsonState

            val message = if (andFinish) "Workout Saved!" else "Progress Saved"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (andFinish) {
                finish()
            }
        } catch (e: Exception) {
            Log.e("WorkoutLoggerActivity", "Failed to save workout data", e)
            Toast.makeText(this, "Error saving workout!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWorkoutData(): Map<String, List<Map<String, String>>> {
        try {
            val prefs = UserDataManager.getUserPrefs(this, "WorkoutHistory")
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val workoutJson = prefs.getString(today, null)

            if (workoutJson != null) {
                val type = object : TypeToken<Map<String, List<Map<String, String>>>>() {}.type
                return Gson().fromJson(workoutJson, type) ?: emptyMap()
            }
        } catch (e: Exception) {
            Log.e("WorkoutLoggerActivity", "Failed to load workout data", e)
        }
        return emptyMap()
    }
}