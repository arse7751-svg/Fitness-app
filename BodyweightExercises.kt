package com.arsenii.fitnessapp

data class Exercise(
    val name: String,
    val description: String,
    var isExpanded: Boolean = false
)

// Holds bodyweight exercises
object BodyweightExercises {
    val list = listOf(
        Exercise("Pull-ups", "An upper-body strength exercise. Hang from a pull-up bar with your palms facing away from you and your body fully extended. Pull yourself up until your chin is above the bar."),
        Exercise("Triceps Dips", "A compound bodyweight exercise that works the triceps, chest, and shoulders. Use parallel bars or a bench."),
        Exercise("Plank", "An isometric core strength exercise that involves maintaining a position similar to a push-up for the maximum possible time."),
        Exercise("Crunches", "A classic core exercise. Lie on your back with your knees bent and feet flat on the floor. Lift your upper body off the floor."),
        Exercise("Push-ups", "A classic bodyweight exercise. Start in a plank position and lower your body until your chest nearly touches the floor, then push back up."),
        Exercise("Chin-ups", "Similar to pull-ups, but with your palms facing towards you. This variation places more emphasis on the biceps."),
        Exercise("Diamond Push-ups", "A variation of the push-up where you form a diamond shape with your hands. This targets the triceps more directly."),
        Exercise("Hyperextensions", "A lower back exercise. Lie face down on a hyperextension bench and raise your upper body."),
        Exercise("Walking Lunges", "A lunge variation where you step forward into a lunge, then bring your back foot forward to the next lunge."),
        Exercise("Lunges", "A single-leg bodyweight exercise that works your quadriceps, glutes, and hamstrings."),
        Exercise("Bulgarian Split Squat", "A single-leg squat variation where your back foot is elevated on a bench or platform.")
    )
}
