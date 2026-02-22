package com.arsenii.fitnessapp

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import com.google.gson.Gson

class DaySelectionActivity : BaseActivity() {

    private lateinit var workoutPlanName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_selection)

        workoutPlanName = intent.getStringExtra("WORKOUT_PLAN_NAME") ?: ""

        val mondayCheckBox = findViewById<CheckBox>(R.id.mondayCheckBox)
        val tuesdayCheckBox = findViewById<CheckBox>(R.id.tuesdayCheckBox)
        val wednesdayCheckBox = findViewById<CheckBox>(R.id.wednesdayCheckBox)
        val thursdayCheckBox = findViewById<CheckBox>(R.id.thursdayCheckBox)
        val fridayCheckBox = findViewById<CheckBox>(R.id.fridayCheckBox)
        val saturdayCheckBox = findViewById<CheckBox>(R.id.saturdayCheckBox)
        val sundayCheckBox = findViewById<CheckBox>(R.id.sundayCheckBox)
        val saveButton = findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val selectedDays = mutableListOf<String>()
            if (mondayCheckBox.isChecked) selectedDays.add("Monday")
            if (tuesdayCheckBox.isChecked) selectedDays.add("Tuesday")
            if (wednesdayCheckBox.isChecked) selectedDays.add("Wednesday")
            if (thursdayCheckBox.isChecked) selectedDays.add("Thursday")
            if (fridayCheckBox.isChecked) selectedDays.add("Friday")
            if (saturdayCheckBox.isChecked) selectedDays.add("Saturday")
            if (sundayCheckBox.isChecked) selectedDays.add("Sunday")

            if (selectedDays.isNotEmpty()) {
                generateAndSaveWorkout(selectedDays)
                finish()
            } else {
                Toast.makeText(this, "Please select at least one day.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Generates a random workout schedule and saves it
    private fun generateAndSaveWorkout(selectedDays: List<String>) {
        val planData = WorkoutData.workouts[workoutPlanName]
        val generatedPlan = mutableMapOf<String, List<String>>()

        // Handles complex plans like PPL
        if (planData is Map<*, *>) {
            for (workoutType in planData.keys) {
                val workoutTypeString = workoutType as String
                val muscleGroups = planData[workoutTypeString] as? Map<String, List<String>>
                val dailyExercises = mutableListOf<String>()

                if (muscleGroups != null) {
                    val shuffledGroups = muscleGroups.keys.shuffled()
                    var exercisesNeeded = 6
                    var groupIndex = 0
                    
                    // Loop to get a balanced workout of 6 exercises
                    while (exercisesNeeded > 0 && groupIndex < shuffledGroups.size) {
                        val group = shuffledGroups[groupIndex]
                        val exercisesPerGroup = if (shuffledGroups.size - groupIndex == 1) exercisesNeeded else (1..2).random()
                        val exercisesFromGroup = muscleGroups[group]?.shuffled()?.take(exercisesPerGroup)
                        
                        if (exercisesFromGroup != null) {
                            dailyExercises.addAll(exercisesFromGroup)
                            exercisesNeeded -= exercisesFromGroup.size
                        }
                        groupIndex++
                    }
                    generatedPlan[workoutTypeString] = dailyExercises
                }
            }
        } else if (planData is List<*>) {
            // Handles simple, full-body plans
            val shuffledExercises = (planData as List<String>).shuffled().take(6)
            generatedPlan[workoutPlanName] = shuffledExercises
        }

        val gson = Gson()
        val workoutJson = gson.toJson(generatedPlan)
        val daysJson = gson.toJson(selectedDays)

        // Save the workout and its schedule
        val schedulePrefs = UserDataManager.getUserPrefs(this, "GeneratedWorkouts").edit()
        schedulePrefs.putString("${workoutPlanName}_workout", workoutJson)
        schedulePrefs.putString("${workoutPlanName}_days", daysJson)
        schedulePrefs.apply()

        // Set this plan as active
        val activePlanPrefs = UserDataManager.getUserPrefs(this, "ActiveWorkoutPlan").edit()
        activePlanPrefs.putString("activePlan", workoutPlanName)
        activePlanPrefs.apply()

        Toast.makeText(this, "Workout Plan Saved!", Toast.LENGTH_SHORT).show()
    }
}
