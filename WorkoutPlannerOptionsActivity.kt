package com.arsenii.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout

class WorkoutPlannerOptionsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_planner_options)

        val preestablishedBox = findViewById<LinearLayout>(R.id.preestablishedBox)
        val ownPlanBox = findViewById<LinearLayout>(R.id.ownPlanBox)
        val quickWorkoutBox = findViewById<LinearLayout>(R.id.quickWorkoutBox)
        val closeButton = findViewById<ImageView>(R.id.closeButton)

        setBoxClickListener(preestablishedBox, PreestablishedWorkoutsActivity::class.java)
        setBoxClickListener(ownPlanBox, OwnPlansActivity::class.java)
        setBoxClickListener(quickWorkoutBox, QuickWorkoutActivity::class.java)

        closeButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.preestablishedBox).scaleX = 1f
        findViewById<View>(R.id.preestablishedBox).scaleY = 1f
        findViewById<View>(R.id.ownPlanBox).scaleX = 1f
        findViewById<View>(R.id.ownPlanBox).scaleY = 1f
        findViewById<View>(R.id.quickWorkoutBox).scaleX = 1f
        findViewById<View>(R.id.quickWorkoutBox).scaleY = 1f
    }

    private fun setBoxClickListener(view: View, activityClass: Class<*>) {
        view.setOnClickListener { it ->
            it.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction {
                    val intent = Intent(this, activityClass)
                    startActivity(intent)
                }
                .start()
        }
    }
}