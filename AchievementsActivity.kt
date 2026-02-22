package com.arsenii.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AchievementsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        findViewById<ImageView>(R.id.closeButton).setOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.maxWeightsButton).setOnClickListener {
            startActivity(Intent(this, MaxWeightsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.bodyweightTrackerButton).setOnClickListener {
            startActivity(Intent(this, BodyweightActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.oneRepMaxButton).setOnClickListener {
            startActivity(Intent(this, OneRepMaxCalculatorActivity::class.java))
        }

        calculateAndDisplayStats()
    }

    // Reads all workout history to calculate overall stats
    private fun calculateAndDisplayStats() {
        val statsContainer = findViewById<LinearLayout>(R.id.statsContainer)
        val historyPrefs = UserDataManager.getUserPrefs(this, "WorkoutHistory")

        var totalDaysTrained = 0
        var totalWeightLifted = 0f
        var totalReps = 0
        var totalSets = 0

        val gson = Gson()
        val type = object : TypeToken<Map<String, List<Map<String, String>>>>() {}.type

        // Loop all saved history
        for ((_, value) in historyPrefs.all) {
            if (value is String) {
                totalDaysTrained++
                val workoutLogs: Map<String, List<Map<String, String>>> = gson.fromJson(value, type)

                for ((_, sets) in workoutLogs) {
                    totalSets += sets.size
                    for (set in sets) {
                        totalWeightLifted += set["weight"]?.toFloatOrNull() ?: 0f
                        totalReps += set["reps"]?.toIntOrNull() ?: 0
                    }
                }
            }
        }

        addStatView(statsContainer, "Total Days Trained", totalDaysTrained.toString())
        addStatView(statsContainer, "Total Weight Lifted", "$totalWeightLifted kg")
        addStatView(statsContainer, "Total Reps", totalReps.toString())
        addStatView(statsContainer, "Total Sets", totalSets.toString())
    }

    // Creates and adds a new stat view to the layout
    private fun addStatView(container: LinearLayout, title: String, value: String) {
        val statView = LayoutInflater.from(this).inflate(R.layout.achievement_stat_item, container, false)
        statView.findViewById<TextView>(R.id.statTitleTextView).text = title
        statView.findViewById<TextView>(R.id.statValueTextView).text = value
        container.addView(statView)
    }
}
