package com.example.examplecustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MessageView>(R.id.messageView).apply {
            setOnAddReactionClickedListener { this.addReaction("😀",1) }
        }
    }
}