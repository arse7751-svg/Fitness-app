package com.arsenii.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExercisesActivity : BaseActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var sortSpinner: Spinner
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter

    private val allExercises = mutableListOf<Pair<String, String>>()
    private var currentlyDisplayedExercises = listOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        searchEditText = findViewById(R.id.searchEditText)
        sortSpinner = findViewById(R.id.sortSpinner)
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView)
        findViewById<ImageView>(R.id.closeButton).setOnClickListener { finish() }

        // Setup the list
        exerciseAdapter = ExerciseAdapter { clickedExerciseName ->
            // Pass list of names for swiping on next screen
            val currentNames = currentlyDisplayedExercises.map { it.first }
            val index = currentNames.indexOf(clickedExerciseName)

            if (index != -1) {
                val intent = Intent(this, ExerciseDetailActivity::class.java).apply {
                    putStringArrayListExtra("EXERCISE_NAME_LIST", ArrayList(currentNames))
                    putExtra("EXERCISE_INDEX", index)
                }
                startActivity(intent)
            }
        }
        exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        exercisesRecyclerView.adapter = exerciseAdapter

        allExercises.addAll(
            ExerciseDescriptions.descriptions.map {
                it.key.replace("Dumbbell", "Dumbell", ignoreCase = true) to it.value
            }.sortedBy { it.first }
        )

        setupSortSpinner()
        setupSearch()

        updateDisplayedExercises()
    }

    private fun setupSortSpinner() {
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = spinnerAdapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateDisplayedExercises()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateDisplayedExercises()
            }
        })
    }

    // This updates the displayed exercises based on search and sort options
    private fun updateDisplayedExercises() {
        val query = searchEditText.text.toString().replace("dumbbell", "dumbell", ignoreCase = true)
        val sortOption = sortSpinner.selectedItemPosition
        val prefs = UserDataManager.getUserPrefs(this, "ExerciseRatings")

        val filteredList = if (query.isEmpty()) {
            allExercises
        } else {
            val regex = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
            allExercises.filter { (name, description) ->
                regex.containsMatchIn(name) || regex.containsMatchIn(description)
            }
        }

        currentlyDisplayedExercises = when (sortOption) {
            0 -> filteredList
            1 -> filteredList.sortedByDescending { prefs.getFloat(it.first + "_rating", 0f) }
            else -> filteredList
        }

        exerciseAdapter.updateData(currentlyDisplayedExercises, query)
    }
}
