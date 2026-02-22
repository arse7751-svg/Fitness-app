package com.arsenii.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OwnPlansActivity : BaseActivity() {

    private var selectedPlanView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_own_plans)

        val addPlanButton = findViewById<FloatingActionButton>(R.id.addPlanButton)
        addPlanButton.setOnClickListener {
            val intent = Intent(this, CreatePlanActivity::class.java)
            startActivity(intent)
        }

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        displayOwnPlans()
    }

    // Shows all user-created workout plans
    private fun displayOwnPlans() {
        val container = findViewById<LinearLayout>(R.id.ownPlansContainer)
        container.removeAllViews()
        val prefs = UserDataManager.getUserPrefs(this, "CustomWorkouts")

        for ((planName, planJson) in prefs.all) {
            val planView = LayoutInflater.from(this).inflate(R.layout.own_plan_item, container, false)
            val planNameTextView = planView.findViewById<TextView>(R.id.planNameTextView)
            val expandableContent = planView.findViewById<LinearLayout>(R.id.expandableContent)
            val daysTextView = planView.findViewById<TextView>(R.id.daysTextView)
            val activateButton = planView.findViewById<Button>(R.id.activateButton)
            val editButton = planView.findViewById<Button>(R.id.editButton)
            val deleteButton = planView.findViewById<Button>(R.id.deleteButton)

            planNameTextView.text = planName

            val gson = Gson()
            val type = object : TypeToken<Map<String, Set<String>>>() {}.type
            val planData: Map<String, Set<String>> = gson.fromJson(planJson as String, type)
            daysTextView.text = "Days: ${planData.keys.joinToString(", ")}"

            planView.setOnClickListener {
                if (selectedPlanView == it) {
                    expandableContent.visibility = if (expandableContent.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                } else {
                    selectedPlanView?.findViewById<LinearLayout>(R.id.expandableContent)?.visibility = View.GONE
                    selectedPlanView = it
                    expandableContent.visibility = View.VISIBLE
                }
            }

            activateButton.setOnClickListener {
                val activePlanPrefs = UserDataManager.getUserPrefs(this, "ActiveWorkoutPlan").edit()
                activePlanPrefs.putString("activePlan", planName)
                activePlanPrefs.apply()
                Toast.makeText(this, "'$planName' activated!", Toast.LENGTH_SHORT).show()
            }

            editButton.setOnClickListener {
                val intent = Intent(this, CreatePlanActivity::class.java).apply {
                    putExtra("PLAN_NAME", planName)
                }
                startActivity(intent)
            }

            deleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Plan")
                    .setMessage("Are you sure you want to delete '$planName'?")
                    .setPositiveButton("Yes") { _, _ ->
                        prefs.edit().remove(planName).apply()
                        displayOwnPlans() // Refresh the list
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            container.addView(planView)
        }
    }
}
