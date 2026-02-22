package com.arsenii.fitnessapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

class BodyweightActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bodyweight)

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val saveWeightButton = findViewById<Button>(R.id.saveWeightButton)
        val weightInputEditText = findViewById<EditText>(R.id.weightInputEditText)

        closeButton.setOnClickListener {
            finish()
        }

        saveWeightButton.setOnClickListener {
            val weight = weightInputEditText.text.toString()
            if (weight.isNotEmpty()) {
                saveBodyweight(weight)
                // Redraw graph with new data
                setupGraph()
            } else {
                Toast.makeText(this, "Please enter your weight.", Toast.LENGTH_SHORT).show()
            }
        }

        setupGraph()
    }

    // Saves bodyweight for today
    private fun saveBodyweight(weight: String) {
        val prefs = getSharedPreferences("BodyweightHistory", Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        prefs.edit().putString(today, weight).apply()
        Toast.makeText(this, "Weight Saved!", Toast.LENGTH_SHORT).show()
    }

    // Sets up graph with saved data
    private fun setupGraph() {
        val graphView = findViewById<BodyweightGraphView>(R.id.bodyweightChart)
        val prefs = getSharedPreferences("BodyweightHistory", Context.MODE_PRIVATE)
        graphView.setData(prefs.all)
    }
}
