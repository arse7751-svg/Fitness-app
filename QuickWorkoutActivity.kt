package com.arsenii.fitnessapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class QuickWorkoutActivity : BaseActivity() {

    data class Exercise(val name: String, val targetedMuscles: List<String>)

    private val allExercises: List<Exercise> by lazy {
        ExerciseDescriptions.descriptions.map { (name, description) ->
            val muscles = parseMuscles(description)
            Exercise(name, muscles)
        }
    }

    private lateinit var workoutRecyclerView: RecyclerView
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var ratingsPrefs: SharedPreferences
    private var currentWorkout: List<Exercise> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_workout)

        ratingsPrefs = UserDataManager.getUserPrefs(this, "ExerciseRatings")

        val muscleGroupSpinner = findViewById<Spinner>(R.id.muscleGroupSpinner)
        val likedExercisesCheckbox = findViewById<CheckBox>(R.id.likedExercisesCheckbox)
        val dislikedExercisesCheckbox = findViewById<CheckBox>(R.id.dislikedExercisesCheckbox)
        val generateWorkoutButton = findViewById<Button>(R.id.generateWorkoutButton)
        val actionButtonsLayout = findViewById<LinearLayout>(R.id.actionButtonsLayout)
        val tryAgainButton = findViewById<Button>(R.id.tryAgainButton)
        val applyButton = findViewById<Button>(R.id.applyButton)
        val closeButton = findViewById<ImageView>(R.id.closeButton)
        workoutRecyclerView = findViewById(R.id.workoutRecyclerView)

        likedExercisesCheckbox.visibility = View.VISIBLE
        dislikedExercisesCheckbox.visibility = View.VISIBLE

        closeButton.setOnClickListener { finish() }

        // Prevents suggesting muscle groups that were trained yesterday
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val trainedYesterday = getTrainedPrimaryMuscleGroupsFor(yesterday)

        val allMuscleGroups = resources.getStringArray(R.array.muscle_groups)
        val availableMuscleGroups = allMuscleGroups.filter { groupOption ->
            val workoutMuscles = groupOption.split(" & ").toSet()
            trainedYesterday.none { it in workoutMuscles }
        }

        if (availableMuscleGroups.isEmpty()) {
            Toast.makeText(this, "You seem to have trained recently! Rest is important.", Toast.LENGTH_LONG).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableMuscleGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        muscleGroupSpinner.adapter = adapter

        workoutRecyclerView.layoutManager = LinearLayoutManager(this)
        workoutAdapter = WorkoutAdapter(emptyList())
        workoutRecyclerView.adapter = workoutAdapter

        generateWorkoutButton.setOnClickListener {
            if (muscleGroupSpinner.selectedItem == null) {
                Toast.makeText(this, "No available muscle groups to generate a workout.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedOption = muscleGroupSpinner.selectedItem.toString()
            val includeLiked = likedExercisesCheckbox.isChecked
            val excludeDisliked = dislikedExercisesCheckbox.isChecked

            currentWorkout = generateWorkout(selectedOption, includeLiked, excludeDisliked)

            if (currentWorkout.isNotEmpty()) {
                workoutAdapter.updateExercises(currentWorkout)
                actionButtonsLayout.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "No exercises found for the selected criteria.", Toast.LENGTH_SHORT).show()
                actionButtonsLayout.visibility = View.GONE
            }
        }

        tryAgainButton.setOnClickListener {
            generateWorkoutButton.performClick()
        }

        applyButton.setOnClickListener {
            if (currentWorkout.isNotEmpty()) {
                saveAndApplyQuickWorkout()
            }
        }
    }

    private fun saveAndApplyQuickWorkout() {
        val planName = "Quick Workout"
        val today = SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().time)

        val workoutPlan = mapOf(today to currentWorkout.map { it.name }.toSet())

        val prefs = UserDataManager.getUserPrefs(this, "CustomWorkouts")
        val gson = Gson()
        val json = gson.toJson(workoutPlan)

        prefs.edit { putString(planName, json) }

        // Set this plan as active
        val activePlanPrefs = UserDataManager.getUserPrefs(this, "ActiveWorkoutPlan")
        activePlanPrefs.edit { putString("activePlan", planName) }

        Toast.makeText(this, "\"Quick Workout\" applied. This is now your active plan.", Toast.LENGTH_LONG).show()
        finish()
    }

    // Gets the muscle groups trained on a specific date
    private fun getTrainedPrimaryMuscleGroupsFor(date: Calendar): Set<String> {
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date.time)
        val activePlanPrefs = UserDataManager.getUserPrefs(this, "ActiveWorkoutPlan")
        val activePlanName = activePlanPrefs.getString("activePlan", null) ?: return emptySet()
        val customWorkoutsPrefs = UserDataManager.getUserPrefs(this, "CustomWorkouts")
        val planJson = customWorkoutsPrefs.getString(activePlanName, null) ?: return emptySet()
        val gson = Gson()
        val type = object : TypeToken<Map<String, Set<String>>>() {}.type
        val plan: Map<String, Set<String>> = gson.fromJson(planJson, type)
        val exerciseNames = plan[dayOfWeek] ?: return emptySet()

        return exerciseNames.flatMap { exerciseName ->
            val description = ExerciseDescriptions.descriptions[exerciseName] ?: ""
            parseMuscles(description)
        }.map { mapToPrimaryGroup(it) }.toSet()
    }

    // Converts detailed muscle names to broad groups
    private fun mapToPrimaryGroup(muscle: String): String {
        return when {
            muscle.contains("Pectoralis", ignoreCase = true) || muscle.contains("chest", ignoreCase = true) -> "Chest"
            muscle.contains("Latissimus", ignoreCase = true) || muscle.contains("back", ignoreCase = true) || muscle.contains("rhomboids", ignoreCase = true) -> "Back"
            muscle.contains("glutes", ignoreCase = true) || muscle.contains("Quadriceps", ignoreCase = true) || muscle.contains("hamstrings", ignoreCase = true) || muscle.contains("Calf", ignoreCase = true) -> "Legs"
            muscle.contains("deltoids", ignoreCase = true) || muscle.contains("trapezius", ignoreCase = true) || muscle.contains("Shoulders", ignoreCase = true) -> "Shoulders"
            muscle.contains("biceps", ignoreCase = true) || muscle.contains("triceps", ignoreCase = true) || muscle.contains("forearms", ignoreCase = true) || muscle.contains("brachialis", ignoreCase = true) -> "Arms"
            muscle.contains("abdominis", ignoreCase = true) || muscle.contains("obliques", ignoreCase = true) || muscle.contains("core", ignoreCase = true) -> "Abs"
            else -> "Unknown"
        }
    }

    // Extracts muscle names from an exercise description
    private fun parseMuscles(description: String): List<String> {
        val prefix = "Muscles Targeted: "
        return description.split('\n').find { it.startsWith(prefix) }?.substring(prefix.length)?.split(", ")?.map { it.replace(".", "").trim() } ?: emptyList()
    }

    private fun generateWorkout(selectedOption: String, includeLiked: Boolean, excludeDisliked: Boolean): List<Exercise> {
        val targetGroups = when {
            selectedOption.contains(" & ") -> selectedOption.split(" & ")
            selectedOption == "Full Body" -> listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Abs")
            else -> listOf(selectedOption)
        }

        var filteredExercises = allExercises.filter { exercise ->
            val primaryGroups = exercise.targetedMuscles.map { mapToPrimaryGroup(it) }.toSet()
            targetGroups.any { it in primaryGroups }
        }

        if (includeLiked) {
            filteredExercises = filteredExercises.filter { ratingsPrefs.getFloat(it.name + "_rating", 3f) >= 4f }
        }
        if (excludeDisliked) {
            filteredExercises = filteredExercises.filter { ratingsPrefs.getFloat(it.name + "_rating", 3f) > 1f }
        }

        return filteredExercises.shuffled().take(6)
    }

    class WorkoutAdapter(private var exercises: List<Exercise>) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): WorkoutViewHolder {
            val itemView = android.widget.TextView(parent.context)
            itemView.layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            itemView.setPadding(16, 16, 16, 16)
            itemView.setTextColor(android.graphics.Color.WHITE)
            return WorkoutViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
            (holder.itemView as android.widget.TextView).text = exercises[position].name
        }

        override fun getItemCount() = exercises.size

        fun updateExercises(newExercises: List<Exercise>) {
            exercises = newExercises
            notifyDataSetChanged()
        }

        class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}
