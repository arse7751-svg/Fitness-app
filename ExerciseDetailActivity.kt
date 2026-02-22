package com.arsenii.fitnessapp

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView

class ExerciseDetailActivity : BaseActivity() {

    private lateinit var exerciseTitle: String
    private lateinit var notesEditText: EditText
    private lateinit var prefs: SharedPreferences

    // Holds the list of names for swiping
    private var exerciseNameList: List<String>? = null
    private var currentIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_detail)

        // Get data from previous screen
        exerciseNameList = intent.getStringArrayListExtra("EXERCISE_NAME_LIST")
        currentIndex = intent.getIntExtra("EXERCISE_INDEX", -1)

        // Fallback if opened with only one title
        if (exerciseNameList == null) {
            exerciseTitle = intent.getStringExtra("EXERCISE_TITLE") ?: ""
            if (exerciseTitle.isNotEmpty()) {
                exerciseNameList = listOf(exerciseTitle)
                currentIndex = 0
            } else {
                finish()
                return
            }
        }

        setupView()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.closeButton).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.leftArrow).setOnClickListener {
            exerciseNameList?.let {
                if (it.isNotEmpty()) {
                    saveNotes()
                    currentIndex = (currentIndex - 1 + it.size) % it.size
                    setupView()
                }
            }
        }

        findViewById<ImageView>(R.id.rightArrow).setOnClickListener {
            exerciseNameList?.let {
                if (it.isNotEmpty()) {
                    saveNotes()
                    currentIndex = (currentIndex + 1) % it.size
                    setupView()
                }
            }
        }
    }

    private fun setupView() {
        if (currentIndex == -1 || exerciseNameList.isNullOrEmpty()) return

        exerciseTitle = exerciseNameList!![currentIndex]
        prefs = UserDataManager.getUserPrefs(this, "ExerciseRatings")

        val titleTextView = findViewById<TextView>(R.id.exerciseTitleTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        notesEditText = findViewById(R.id.notesEditText)
        val exerciseImageView = findViewById<ImageView>(R.id.exerciseImageView)

        titleTextView.text = exerciseTitle
        descriptionTextView.text = ExerciseDescriptions.descriptions[exerciseTitle] ?: "No description available."

        val imageName = getDrawableNameFor(exerciseTitle)
        val imageResId = resources.getIdentifier(imageName, "drawable", packageName)

        if (imageResId != 0) {
            exerciseImageView.setImageResource(imageResId)
        } else {
            exerciseImageView.setImageResource(R.drawable.bck) // Default image
        }

        ratingBar.rating = prefs.getFloat(exerciseTitle + "_rating", 0f)
        notesEditText.setText(prefs.getString(exerciseTitle + "_notes", ""))

        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            prefs.edit().putFloat(exerciseTitle + "_rating", rating).apply()
        }

        val arrowVisibility = if (exerciseNameList!!.size > 1) View.VISIBLE else View.GONE
        findViewById<ImageView>(R.id.leftArrow).visibility = arrowVisibility
        findViewById<ImageView>(R.id.rightArrow).visibility = arrowVisibility
    }

    private fun saveNotes() {
        if (::notesEditText.isInitialized) {
            prefs.edit().putString(exerciseTitle + "_notes", notesEditText.text.toString()).apply()
        }
    }

    private fun getDrawableNameFor(exerciseName: String): String {
        // Special cases for typos in filenames
        if (exerciseName == "Reverse Pec-Deck") {
            return "reverse_peck_deck"
        }
        return exerciseName.lowercase().trim()
            .replace(Regex("[^a-z0-9]+"), "_")
            .removeSuffix("_")
            .removePrefix("_")
    }

    override fun onPause() {
        super.onPause()
        saveNotes()
    }
}
