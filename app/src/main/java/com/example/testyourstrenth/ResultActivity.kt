package com.example.testyourstrenth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt
import android.widget.Toast


class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val scoreText = findViewById<TextView>(R.id.scoreText)
        val analysisText = findViewById<TextView>(R.id.analysisText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val retryBtn = findViewById<Button>(R.id.retryBtn)
        val homeBtn = findViewById<Button>(R.id.homeBtn)

        // Get total scores
        val totalScore = intent.getIntExtra("score", 0)
        val totalQuestions = intent.getIntExtra("total", 1)

        // Get subject-wise data
        val subjectScores = intent.getSerializableExtra("subjectScores") as? HashMap<String, Int> ?: hashMapOf()
        val subjectTotals = intent.getSerializableExtra("subjectTotals") as? HashMap<String, Int> ?: hashMapOf()

        // Overall performance
        val overallPercent = ((totalScore.toDouble() / totalQuestions) * 100).roundToInt()
        scoreText.text = "Your Score: $totalScore / $totalQuestions  ($overallPercent%)"
        progressBar.progress = overallPercent

        // Build detailed analysis text
        val builder = StringBuilder()
        builder.append("📊 Detailed Analysis\n\n")

        for (subject in subjectTotals.keys) {
            val correct = subjectScores[subject] ?: 0
            val total = subjectTotals[subject] ?: 0
            val percent = if (total > 0) (correct * 100) / total else 0
            builder.append("$subject: $correct / $total  ($percent%)\n")
        }

        builder.append("\n🏁 Overall: $totalScore / $totalQuestions  ($overallPercent%)\n\n")

        // Generate compliments per subject
        builder.append("💬 Feedback:\n")
        for (subject in subjectTotals.keys) {
            val correct = subjectScores[subject] ?: 0
            val total = subjectTotals[subject] ?: 0
            val percent = if (total > 0) (correct * 100) / total else 0
            when {
                percent >= 80 -> builder.append("⭐ Excellent in $subject!\n")
                percent >= 60 -> builder.append("👍 Good understanding of $subject.\n")
                percent >= 40 -> builder.append("📘 Average in $subject, can improve.\n")
                else -> builder.append("⚠ Needs improvement in $subject.\n")
            }
        }

        // Display the final result
        analysisText.text = builder.toString()

        // Buttons
        retryBtn.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
            finish()
        }

        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Optional: Friendly toast summary
        when {
            overallPercent >= 80 -> Toast.makeText(this, "Outstanding performance! 🏆", Toast.LENGTH_SHORT).show()
            overallPercent >= 60 -> Toast.makeText(this, "Good job! Keep it up 🔥", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, "Don’t worry! Practice makes perfect 🌱", Toast.LENGTH_SHORT).show()
        }
    }
}




