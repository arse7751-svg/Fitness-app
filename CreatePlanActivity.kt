package com.arsenii.fitnessapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CreatePlanActivity : BaseActivity() {

    // Holds the workout plan data
    private val plan = mutableMapOf<String, MutableSet<String>>()
    private var selectedDay: String? = null
    private var initialPlanState: String? = null // For unsaved changes check
    private var originalPlanName: String? = null
    private lateinit var exerciseAdapter: CheckableExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plan)

        val planNameEditText = findViewById<EditText>(R.id.planNameEditText)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val exercisesRecyclerView = findViewById<RecyclerView>(R.id.exercisesRecyclerView)
        val savePlanButton = findViewById<Button>(R.id.savePlanButton)
        val daysContainer = findViewById<LinearLayout>(R.id.daysContainer)
        val closeButton = findViewById<ImageView>(R.id.closeButton)

        // Setup list of checkable exercises
        val allExercises = ExerciseDescriptions.descriptions.keys.sorted()
        exerciseAdapter = CheckableExerciseAdapter(allExercises) { exerciseName, isChecked ->
            selectedDay?.let {
                if (isChecked) {
                    plan.getOrPut(it) { mutableSetOf() }.add(exerciseName)
                } else {
                    plan[it]?.remove(exerciseName)
                }
            }
        }
        exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        exercisesRecyclerView.adapter = exerciseAdapter

        // If editing, load existing plan
        originalPlanName = intent.getStringExtra("PLAN_NAME")
        if (originalPlanName != null) {
            loadPlanForEditing(originalPlanName!!)
            planNameEditText.setText(originalPlanName)
        }

        // Create buttons for each day
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (day in daysOfWeek) {
            val dayButton = Button(this)
            dayButton.text = day
            dayButton.setOnClickListener {
                selectedDay = day
                Toast.makeText(this, "Selected $day", Toast.LENGTH_SHORT).show()
                // Update adapter to show checked exercises for this day
                exerciseAdapter.setSelected(plan[day] ?: emptySet())
            }
            daysContainer.addView(dayButton)
        }

        if (originalPlanName != null) {
            val firstDayWithExercises = daysOfWeek.firstOrNull { plan.containsKey(it) && plan[it]!!.isNotEmpty() } ?: daysOfWeek.first()
            selectedDay = firstDayWithExercises
            exerciseAdapter.setSelected(plan[firstDayWithExercises] ?: emptySet())
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                exerciseAdapter.filter(s.toString())
            }
        })

        closeButton.setOnClickListener {
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog()
            } else {
                finish()
            }
        }

        savePlanButton.setOnClickListener {
            savePlan()
        }

        // Capture initial state for unsaved changes check
        initialPlanState = getCurrentPlanStateAsJson()
    }

    // Saves the plan to device storage
    private fun savePlan() {
        val newPlanName = findViewById<EditText>(R.id.planNameEditText).text.toString().trim()
        if (newPlanName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for your plan.", Toast.LENGTH_SHORT).show()
            return
        }
        val filteredPlan = plan.filterValues { it.isNotEmpty() }
        if (filteredPlan.isEmpty()) {
            Toast.makeText(this, "Please select at least one day and exercise.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = UserDataManager.getUserPrefs(this, "CustomWorkouts")

        // Prevent overwriting a different plan
        if (prefs.contains(newPlanName) && newPlanName != originalPlanName) {
            Toast.makeText(this, "A plan with this name already exists.", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert plan to JSON string for storage
        val gson = Gson()
        val json = gson.toJson(filteredPlan)

        prefs.edit {
            // If renamed, remove the old version
            if (originalPlanName != null && originalPlanName != newPlanName) {
                remove(originalPlanName)
            }
            putString(newPlanName, json)
        }

        Toast.makeText(this, "Plan saved successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun hasUnsavedChanges(): Boolean {
        return getCurrentPlanStateAsJson() != initialPlanState
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Unsaved Changes")
            .setMessage("Are you sure you want to leave without saving changes?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    // Converts plan to a consistent JSON string for comparison
    private fun getCurrentPlanStateAsJson(): String {
        val gson = Gson()
        val sortedPlan = plan.toSortedMap().mapValues { it.value.sorted() }
        return gson.toJson(sortedPlan.filterValues { it.isNotEmpty() })
    }

    // Loads a plan from storage for editing
    private fun loadPlanForEditing(planName: String) {
        val prefs = UserDataManager.getUserPrefs(this, "CustomWorkouts")
        val json = prefs.getString(planName, null)
        if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableMap<String, MutableSet<String>>>() {}.type
            val loadedPlan: Map<String, Set<String>> = gson.fromJson(json, type)
            plan.clear()
            loadedPlan.forEach { (day, exercises) ->
                plan[day] = exercises.toMutableSet()
            }
        }
    }
}
