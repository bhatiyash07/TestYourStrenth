package com.example.testyourstrenth

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class AboutActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val aboutText = findViewById<TextView>(R.id.aboutText)
        aboutText.text = """
            📘 TestYourStrength

            This app helps students test their knowledge 
            through mock tests and provides detailed analysis 
            of their strengths and weaknesses.

            💡 Technologies Used:
            Kotlin | Firebase | Android Studio

            👨‍💻 Developed by: Yash Bhati
        """.trimIndent()
    }
}
