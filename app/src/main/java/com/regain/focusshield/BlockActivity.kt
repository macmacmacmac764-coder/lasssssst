package com.regain.focusshield

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView

class BlockActivity : Activity() {
    private lateinit var timerView: TextView
    private val handler = Handler(mainLooper)

    private val tick = object : Runnable {
        override fun run() {
            val left = Prefs.end(this@BlockActivity) - System.currentTimeMillis()
            if (!Prefs.enabled(this@BlockActivity) || left <= 0L) {
                finish()
                return
            }
            val sec = left / 1000L
            timerView.text = String.format("%02d:%02d", sec / 60L, sec % 60L)
            handler.postDelayed(this, 500L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block)
        timerView = findViewById(R.id.blockTimer)
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        handler.removeCallbacks(tick)
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }
}
