package com.example.testyourstrenth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // If not logged in, redirect to Login
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val startBtn = findViewById<Button>(R.id.startTestBtn)
        val aboutBtn = findViewById<Button>(R.id.aboutBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        // Start Quiz
        startBtn.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }

        // About Section
        aboutBtn.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<Button>(R.id.leaderboardBtn).setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }


        // Logout Functionality
        logoutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        // Re-check user authentication
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

/*
        data class Question(
            val question: String = "",
            val options: List<String> = emptyList(),
            val correctIndex: Int = 0,
            val subject: String = ""
        )



        val questionList = listOf()


        val db = FirebaseFirestore.getInstance()

        for (q in questionList) {
            db.collection("questions").add(q)
                .addOnSuccessListener {
                    Log.d("Firestore", "Question added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding question", e)
                }
        }

*/
    }
}
