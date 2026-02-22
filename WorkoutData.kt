package com.arsenii.fitnessapp

object WorkoutData {

    // The master list of exercises, now structured by muscle group.
    private val pushExercises = mapOf(
        "Chest" to listOf("Bench Press", "Incline Dumbell Press", "Dumbell Flyes", "Cable Crossover", "Push-ups", "Incline Bench Press", "Decline Bench Press", "Dumbell Bench Press"),
        "Shoulders" to listOf("Overhead Press", "Lateral Raises", "Arnold Press", "Seated Dumbell Shoulder Press", "Front Raises", "Upright Row"),
        "Triceps" to listOf("Triceps Dips", "Skull Crushers", "Triceps Pushdown", "Diamond Push-ups", "Close-Grip Bench Press")
    )

    private val pullExercises = mapOf(
        "Back" to listOf("Pull-ups", "Barbell Row", "Lat Pulldowns", "Seated Cable Row", "T-Bar Row", "Single-Arm Dumbell Row", "Hyperextensions", "Bent Over Dumbell Row"),
        "Biceps" to listOf("Dumbell Curls", "Hammer Curls", "Preacher Curls", "Chin-ups", "Concentration Curl"),
        "Rear Delts/Traps" to listOf("Face Pulls", "Shrugs", "Dumbell Pullover", "Reverse Pec-Deck")
    )

    private val legExercises = mapOf(
        "Quads" to listOf("Squat", "Leg Press", "Leg Extensions", "Goblet Squat", "Hack Squat", "Sissy Squat"),
        "Hamstrings & Glutes" to listOf("Deadlift", "Romanian Deadlift", "Good Mornings", "Lunges", "Walking Lunges", "Bulgarian Split Squat", "Leg Curls", "Glute Bridge"),
        "Calves" to listOf("Calf Raises", "Seated Calf Raise")
    )
    
    private val allMuscleGroups = pushExercises + pullExercises + legExercises

    val workouts = mapOf(
        "Push-Pull-Legs (PPL)" to mapOf(
            "Push" to pushExercises,
            "Pull" to pullExercises,
            "Legs" to legExercises
        ),
        "Upper/Lower Split" to mapOf(
            "Upper" to pushExercises + pullExercises, // Combine for upper body day
            "Lower" to legExercises
        ),
        "Full Body Strength" to listOf("Squat", "Bench Press", "Barbell Row", "Overhead Press", "Deadlift", "Pull-ups"), // Full body is an exception
        "5-Day Split" to mapOf(
            "Chest" to mapOf("Chest" to pushExercises["Chest"]!!),
            "Back" to mapOf("Back" to pullExercises["Back"]!!),
            "Shoulders" to mapOf("Shoulders" to pushExercises["Shoulders"]!!),
            "Legs" to legExercises,
            "Arms" to mapOf("Biceps" to pullExercises["Biceps"]!!, "Triceps" to pushExercises["Triceps"]!!)
        )
    )

    fun getMuscleGroupForExercise(exerciseName: String): String? {
        for ((group, exercises) in allMuscleGroups) {
            if (exercises.contains(exerciseName)) {
                return group
            }
        }
        return null
    }
    
    fun getExercisesForMuscleGroup(muscleGroup: String): List<String> {
        return allMuscleGroups[muscleGroup] ?: emptyList()
    }
}
