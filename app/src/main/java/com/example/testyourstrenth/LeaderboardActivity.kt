package com.example.testyourstrenth

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.content.Intent
import com.google.android.material.button.MaterialButton


class LeaderboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val container = findViewById<LinearLayout>(R.id.leaderboardContainer)
        val titleText = findViewById<TextView>(R.id.titleText)

        val homeBtn = findViewById<MaterialButton>(R.id.homeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        titleText.text = "🏆 Top 3 Performers"

        db.collection("leaderboard")
            .orderBy("percentage", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    val noData = TextView(this)
                    noData.text = "No leaderboard data yet!"
                    noData.textSize = 18f
                    noData.setTextColor(Color.WHITE)
                    noData.gravity = Gravity.CENTER
                    container.addView(noData)
                    return@addOnSuccessListener
                }

                var rank = 1
                for (doc in result) {
                    // ✅ Fetch nickname from Gmail (saved earlier as “nickname”)
                    val nickname = doc.getString("nickname") ?: "Anonymous"
                    val percent = doc.getDouble("percentage")?.toInt() ?: 0

                    // 🧩 Create Card View for each performer
                    val card = MaterialCardView(this)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 16, 0, 0)
                    card.layoutParams = params
                    card.setCardBackgroundColor(Color.parseColor("#2C2C2C"))
                    card.radius = 24f
                    card.cardElevation = 10f
                    card.strokeColor = when (rank) {
                        1 -> Color.parseColor("#FFD700") // gold
                        2 -> Color.parseColor("#C0C0C0") // silver
                        3 -> Color.parseColor("#CD7F32") // bronze
                        else -> Color.WHITE
                    }
                    card.strokeWidth = if (rank == 1) 6 else 3

                    val textView = TextView(this)
                    textView.textSize = 20f
                    textView.setPadding(24, 24, 24, 24)
                    textView.setTextColor(Color.WHITE)
                    textView.gravity = Gravity.CENTER_VERTICAL

                    val rankEmoji = when (rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "🏅"
                    }

                    textView.text = "$rankEmoji  #$rank  $nickname  —  $percent%"

                    card.addView(textView)
                    container.addView(card)
                    rank++
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
    }
}
