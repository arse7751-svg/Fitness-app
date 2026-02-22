package com.arsenii.fitnessapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : BaseActivity() {

    private lateinit var quoteTextView: TextView
    private lateinit var quotes: Array<String>
    private val handler = Handler(Looper.getMainLooper())
    private val updateQuoteRunnable = object : Runnable {
        override fun run() {
            if (::quotes.isInitialized && quotes.isNotEmpty()) {
                quoteTextView.text = quotes.random()
            }
            // Changes quote every 10 seconds
            handler.postDelayed(this, 10000)
        }
    }

    private lateinit var plannerBox: LinearLayout
    private lateinit var exercisesBox: LinearLayout
    private lateinit var historyBox: LinearLayout
    private lateinit var achievementsBox: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        quoteTextView = findViewById(R.id.quoteTextView)
        quotes = resources.getStringArray(R.array.motivational_quotes)

        plannerBox = findViewById(R.id.plannerBox)
        exercisesBox = findViewById(R.id.exercisesBox)
        historyBox = findViewById(R.id.historyBox)
        achievementsBox = findViewById(R.id.achievementsBox)

        setBoxClickListener(plannerBox) { startActivity(Intent(this, WorkoutPlannerOptionsActivity::class.java)) }
        setBoxClickListener(exercisesBox) { startActivity(Intent(this, ExercisesActivity::class.java)) }
        setBoxClickListener(historyBox) { startActivity(Intent(this, HistoryActivity::class.java)) }
        setBoxClickListener(achievementsBox) { startActivity(Intent(this, AchievementsActivity::class.java)) }

        setupWeeklyCalendar()
    }

    override fun onResume() {
        super.onResume()
        displayTodaysWorkout()
        handler.post(updateQuoteRunnable)
        // Reset the scale of the boxes
        plannerBox.scaleX = 1f
        plannerBox.scaleY = 1f
        exercisesBox.scaleX = 1f
        exercisesBox.scaleY = 1f
        historyBox.scaleX = 1f
        historyBox.scaleY = 1f
        achievementsBox.scaleX = 1f
        achievementsBox.scaleY = 1f
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateQuoteRunnable)
    }

    private fun setBoxClickListener(view: View, action: () -> Unit) {
        view.setOnClickListener {
            it.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                // Launch the new activity after the animation completes
                handler.postDelayed({
                    action()
                }, 50) // A small delay to ensure the animation is visible
            }.start()
        }
    }

    private fun setupWeeklyCalendar() {
        val calendarContainer = findViewById<LinearLayout>(R.id.calendarContainer)
        calendarContainer.removeAllViews()
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault())

        val today = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for (i in 0..6) {
            val dayView = LayoutInflater.from(this).inflate(R.layout.calendar_day_layout, calendarContainer, false)
            val dayOfWeekTextView = dayView.findViewById<TextView>(R.id.dayOfWeekTextView)
            val dayOfMonthTextView = dayView.findViewById<TextView>(R.id.dayOfMonthTextView)

            dayOfWeekTextView.text = dayOfWeekFormat.format(calendar.time)
            dayOfMonthTextView.text = dayOfMonthFormat.format(calendar.time)
            dayOfWeekTextView.setTextColor(Color.BLACK)
            dayOfMonthTextView.setTextColor(Color.BLACK)

            // Highlight today's date
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                dayOfMonthTextView.background = ContextCompat.getDrawable(this, R.drawable.current_day_background)
                dayOfWeekTextView.setTextColor(Color.WHITE)
                dayOfMonthTextView.setTextColor(Color.WHITE)
            }

            calendarContainer.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // Checks for and displays today's workout if one is scheduled
    private fun displayTodaysWorkout() {
        val activePlanPrefs = UserDataManager.getUserPrefs(this, "ActiveWorkoutPlan")
        val activePlanName = activePlanPrefs.getString("activePlan", null)
        val todaysWorkoutTextView = findViewById<TextView>(R.id.todaysWorkoutTextView)

        todaysWorkoutTextView.visibility = View.GONE
        todaysWorkoutTextView.setOnClickListener(null)

        if (activePlanName != null) {
            val generatedWorkoutPrefs = UserDataManager.getUserPrefs(this, "GeneratedWorkouts")
            val workoutJson = generatedWorkoutPrefs.getString("${activePlanName}_workout", null)
            val daysJson = generatedWorkoutPrefs.getString("${activePlanName}_days", null)

            if (workoutJson != null && daysJson != null) {
                handlePreestablishedPlan(workoutJson, daysJson)
            } else {
                val customWorkoutPrefs = UserDataManager.getUserPrefs(this, "CustomWorkouts")
                val customPlanJson = customWorkoutPrefs.getString(activePlanName, null)
                if (customPlanJson != null) {
                    handleCustomPlan(customPlanJson)
                }
            }
        }
    }

    // Handles pre-established plans like PPL
    private fun handlePreestablishedPlan(workoutJson: String, daysJson: String) {
        val gson = Gson()
        val workoutType = object : TypeToken<Map<String, List<String>>>() {}.type
        val daysType = object : TypeToken<List<String>>() {}.type
        val generatedWorkout: Map<String, List<String>> = gson.fromJson(workoutJson, workoutType)
        val scheduledDays: List<String> = gson.fromJson(daysJson, daysType)

        val dayOfWeekName = SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().time)

        if (scheduledDays.contains(dayOfWeekName)) {
            val todayIndex = scheduledDays.indexOf(dayOfWeekName)
            val workoutTypes = generatedWorkout.keys.toList()
            val workoutTypeIndex = todayIndex % workoutTypes.size
            val workoutForToday = generatedWorkout[workoutTypes[workoutTypeIndex]]

            if (workoutForToday != null && workoutForToday.isNotEmpty()) {
                displayWorkout(ArrayList(workoutForToday))
            }
        }
    }

    // Handles user-created custom plans
    private fun handleCustomPlan(planJson: String) {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Set<String>>>() {}.type
        val customPlan: Map<String, Set<String>> = gson.fromJson(planJson, type)

        val dayOfWeekName = SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().time)
        val exercisesForToday = customPlan[dayOfWeekName]

        if (exercisesForToday != null && exercisesForToday.isNotEmpty()) {
            displayWorkout(ArrayList(exercisesForToday))
        }
    }

    private fun displayWorkout(exercises: ArrayList<String>) {
        val todaysWorkoutTextView = findViewById<TextView>(R.id.todaysWorkoutTextView)
        todaysWorkoutTextView.visibility = View.VISIBLE

        val title = "Today's Workout:\n"
        val exerciseList = exercises.joinToString("\n")
        val fullText = SpannableStringBuilder(title).append(exerciseList)
        fullText.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        todaysWorkoutTextView.text = fullText

        setBoxClickListener(todaysWorkoutTextView) {
            val intent = Intent(this, WorkoutLoggerActivity::class.java).apply {
                putStringArrayListExtra("EXERCISES", exercises)
            }
            startActivity(intent)
        }
    }
}
