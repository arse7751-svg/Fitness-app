package com.arsenii.fitnessapp

import android.content.Context
import android.os.Bundle
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val closeButton = findViewById<ImageView>(R.id.closeButton)

        closeButton.setOnClickListener {
            finish()
        }

        // If a date was passed from another screen, show it
        val selectedDate = intent.getStringExtra("SELECTED_DATE")
        if (selectedDate != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(selectedDate)
            if (date != null) {
                calendarView.date = date.time
                displayWorkoutForDate(selectedDate)
            }
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            displayWorkoutForDate(date)
        }
    }

    // Shows the workout for a specific date
    private fun displayWorkoutForDate(date: String) {
        val workoutDetailsTextView = findViewById<TextView>(R.id.workoutDetailsTextView)
        val prefs = UserDataManager.getUserPrefs(this, "WorkoutHistory")
        val workoutJson = prefs.getString(date, null)

        if (workoutJson != null) {
            val gson = Gson()
            val type = object : TypeToken<Map<String, List<Map<String, String>>>>() {}.type
            val workoutLogs: Map<String, List<Map<String, String>>> = gson.fromJson(workoutJson, type)

            val workoutDetails = StringBuilder()
            for ((exercise, sets) in workoutLogs) {
                workoutDetails.append("$exercise:\n")
                for (set in sets) {
                    workoutDetails.append("  - Weight: ${set["weight"]}, Reps: ${set["reps"]}\n")
                }
            }
            workoutDetailsTextView.text = workoutDetails.toString()
        } else {
            workoutDetailsTextView.text = "No workout recorded for this day."
        }
    }
}
