package com.arsenii.fitnessapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class CreateNewUsernameActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.createnewusername)

        val newUsername = findViewById<EditText>(R.id.newUsername)
        val newPassword = findViewById<TextInputEditText>(R.id.newPassword)
        val confirmPassword = findViewById<TextInputEditText>(R.id.confirmPassword)
        val createNewUserButton = findViewById<Button>(R.id.createNewUserButton)
        val closeButton = findViewById<ImageView>(R.id.closeButton)

        closeButton.setOnClickListener {
            finish()
        }

        createNewUserButton.setOnClickListener {
            val username = newUsername.text.toString()
            val password = newPassword.text.toString()
            val confirm = confirmPassword.text.toString()

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            if (sharedPreferences.contains(username)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save new user
            with(sharedPreferences.edit()) {
                putString(username, password)
                apply()
            }

            Toast.makeText(this, "User created successfully. Please log in.", Toast.LENGTH_LONG).show()
            
            // Return to main login screen
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
