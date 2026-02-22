package com.arsenii.fitnessapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.round

class OneRepMaxCalculatorActivity : BaseActivity() {

    private lateinit var weightEditText: EditText
    private lateinit var repsEditText: EditText
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_rep_max_calculator)

        weightEditText = findViewById(R.id.weightEditText)
        repsEditText = findViewById(R.id.repsEditText)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        resultTextView = findViewById(R.id.resultTextView)
        val closeButton = findViewById<ImageView>(R.id.closeButton)

        calculateButton.setOnClickListener {
            calculateOneRepMax()
        }

        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun calculateOneRepMax() {
        val weightStr = weightEditText.text.toString()
        val repsStr = repsEditText.text.toString()

        if (weightStr.isEmpty() || repsStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toFloatOrNull()
        val reps = repsStr.toIntOrNull()

        if (weight == null || reps == null || weight <= 0 || reps <= 0) {
            Toast.makeText(this, "Please enter valid, positive numbers", Toast.LENGTH_SHORT).show()
            return
        }

        // Epley Formula for 1RM
        val oneRepMax = weight * (1 + reps / 30f)
        val roundedOneRepMax = round(oneRepMax * 10) / 10f

        resultTextView.text = "Estimated 1RM: $roundedOneRepMax kg"
        resultTextView.visibility = View.VISIBLE
    }
}
