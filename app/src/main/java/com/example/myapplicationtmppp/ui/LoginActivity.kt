package com.example.myapplicationtmppp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.MainActivity
import com.example.myapplicationtmppp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Check if the user is already logged in
        if (auth.currentUser != null) {
            // If user is logged in, redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Login button logic
        binding.loginButton.setOnClickListener {
            val email = binding.emailField.text.toString().trim()
            val password = binding.passwordField.text.toString().trim()

            // Check for empty fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign in with Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If login is successful, show a message and redirect to MainActivity
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Show error message if login failed
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Redirect to RegisterActivity if user doesn't have an account
        binding.registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
