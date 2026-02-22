package com.arsenii.fitnessapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class PreestablishedWorkoutsActivity : BaseActivity() {

    private var selectedWorkout: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preestablished_workouts)

        findViewById<ImageView>(R.id.closeButton).setOnClickListener { finish() }

        val container = findViewById<ViewGroup>(R.id.workoutsContainer)
        for (i in 0 until container.childCount) {
            val workoutLayout = container.getChildAt(i)
            if (workoutLayout is ViewGroup) {
                val selectButton = workoutLayout.findViewWithTag<Button>("selectButton")
                val titleTextView = workoutLayout.getChildAt(0) as? TextView

                if (titleTextView != null) {
                    workoutLayout.setOnClickListener { onWorkoutClicked(it) }
                }

                if (selectButton != null && titleTextView != null) {
                    selectButton.setOnClickListener {
                        val intent = Intent(this, DaySelectionActivity::class.java).apply {
                            putExtra("WORKOUT_PLAN_NAME", titleTextView.text.toString())
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun onWorkoutClicked(view: View) {
        if (selectedWorkout != view) {
            selectedWorkout?.let { animateBox(it, false) }
            selectedWorkout = view
            animateBox(view, true)
        }
    }

    private fun animateBox(view: View, expand: Boolean) {
        val scale = if (expand) 1.05f else 1.0f
        ObjectAnimator.ofFloat(view, "scaleX", scale).setDuration(200).start()
        ObjectAnimator.ofFloat(view, "scaleY", scale).setDuration(200).start()
    }
}
