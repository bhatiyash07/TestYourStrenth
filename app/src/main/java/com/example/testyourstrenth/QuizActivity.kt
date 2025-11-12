package com.example.testyourstrenth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.content.res.ColorStateList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class QuizActivity : AppCompatActivity() {

    data class Question(
        val question: String = "",
        val options: List<String> = emptyList(),
        val correctIndex: Int = 0,
        val subject: String = ""
    )
    private val questions = mutableListOf<Question>()
    private val subjectScores = mutableMapOf<String, Int>()
    private val subjectTotals = mutableMapOf<String, Int>()

    private var currentIndex = 0
    private var score = 0
    private lateinit var questionText: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var nextBtn: Button
    private val radioButtons = mutableListOf<RadioButton>()
    private lateinit var progressBar: ProgressBar
    private lateinit var db: FirebaseFirestore

    private var hasMoved = false
    private val handler = Handler(Looper.getMainLooper())
    private var nextRunnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionText = findViewById(R.id.questionText)
        optionsGroup = findViewById(R.id.optionsGroup)
        nextBtn = findViewById(R.id.nextBtn)
        progressBar = findViewById(R.id.progressBar)


        db = FirebaseFirestore.getInstance()
        loadQuestionsFromFirebase()
    }

    private fun loadQuestionsFromFirebase() {
        progressBar.visibility = ProgressBar.VISIBLE

        db.collection("questions").get()
            .addOnSuccessListener { result ->
                questions.clear()
                val allQuestions = mutableListOf<Question>()

                // 🔹 Fetch all questions from Firebase
                for (document in result) {
                    val question = document.getString("question") ?: ""
                    val options = document.get("options") as? List<String> ?: emptyList()
                    val correctIndex = (document.getLong("correctIndex") ?: 0L).toInt()
                    val subject = document.getString("subject") ?: "General"

                    allQuestions.add(Question(question, options, correctIndex, subject))
                }

                // 🔹 Group questions by subject
                val groupedBySubject = allQuestions.groupBy { it.subject }

                // 🔹 Select exactly 2 random questions per subject
                val selectedQuestions = mutableListOf<Question>()
                for ((_, qList) in groupedBySubject) {
                    selectedQuestions.addAll(qList.shuffled().take(2))
                }

                // 🔹 Shuffle overall list for random order
                questions.addAll(selectedQuestions.shuffled())

                progressBar.visibility = ProgressBar.GONE

                if (questions.isNotEmpty()) {
                    loadQuestion()
                } else {
                    Toast.makeText(this, "No questions found in Firebase!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadQuestion() {
        val q = questions[currentIndex]
        questionText.text = q.question
        optionsGroup.removeAllViews()
        radioButtons.clear()


        val subject = questions[currentIndex].subject
        subjectTotals[subject] = (subjectTotals[subject] ?: 0) + 1


        nextBtn.isEnabled = false
        nextBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD")) // Gray (inactive)

        for (i in q.options.indices) {
            val rb = RadioButton(this)
            rb.text = q.options[i]
            rb.id = i
            rb.setTextColor(Color.BLACK)
            rb.textSize = 18f
            rb.buttonTintList = ColorStateList.valueOf(Color.parseColor("#6200EE"))
            optionsGroup.addView(rb)
            radioButtons.add(rb)
        }

        optionsGroup.setOnCheckedChangeListener { _, checkedId ->
            nextBtn.isEnabled = true
            nextBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#6200EE"))
            handleAnswer(checkedId, q.correctIndex)
        }
    }


    private fun handleAnswer(selectedId: Int, correctIndex: Int) {
        // Disable all options after selection
        for (rb in radioButtons) rb.isEnabled = false
        val selectedRb = radioButtons[selectedId]

        if (selectedId == correctIndex) {
            selectedRb.buttonTintList = ColorStateList.valueOf(Color.parseColor("#388E3C")) // Green
            score++
            val subject = questions[currentIndex].subject
            subjectScores[subject] = (subjectScores[subject] ?: 0) + 1

        } else {
            selectedRb.buttonTintList = ColorStateList.valueOf(Color.parseColor("#D32F2F")) // Red
            radioButtons[correctIndex].buttonTintList = ColorStateList.valueOf(Color.parseColor("#388E3C")) // Green
        }

        // Enable "Next" button manually
        nextBtn.isEnabled = true
        nextBtn.text = "Next Question"
        nextBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#388E3C"))

        hasMoved = false // reset the flag

        // Cancel any old timer
        nextRunnable?.let { handler.removeCallbacks(it) }

        // ✅ Manual skip
        nextBtn.setOnClickListener {
            if (!hasMoved) {
                hasMoved = true
                cancelTimer() // stop any existing timer
                moveToNextQuestion()
            }
        }

        // ✅ Automatic skip after delay
        nextRunnable = Runnable {
            if (!hasMoved) {
                hasMoved = true
                moveToNextQuestion()
            }
        }
        handler.postDelayed(nextRunnable!!, 5000) // ⏱ 5 seconds
    }


    private fun moveToNextQuestion() {
        currentIndex++
        if (currentIndex < questions.size) {
            loadQuestion()
        } else {
            val intent = Intent(this, ResultActivity::class.java)
            // leaderboard
            saveToLeaderboard(score, questions.size)



            // detailed anylisis
            intent.putExtra("score", score)
            intent.putExtra("total", questions.size)
            intent.putExtra("subjectScores", HashMap(subjectScores))
            intent.putExtra("subjectTotals", HashMap(subjectTotals))
            startActivity(intent)
            finish()
        }
    }
    private fun cancelTimer() {
        nextRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun saveToLeaderboard(score: Int, total: Int) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val percentage = ((score.toDouble() / total) * 100).toInt()
        val email = user?.email ?: "guest@example.com"
        val nickname = email.substringBefore("@") // Extract nickname from Gmail

        val data = hashMapOf(
            "uid" to (user?.uid ?: UUID.randomUUID().toString()),
            "email" to email,
            "nickname" to nickname.replaceFirstChar { it.uppercase() },
            "score" to score,
            "total" to total,
            "percentage" to percentage,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("leaderboard").add(data)
    }



}







