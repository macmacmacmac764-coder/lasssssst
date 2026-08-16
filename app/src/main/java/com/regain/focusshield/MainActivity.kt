package com.regain.focusshield

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {

    private var timer: CountDownTimer? = null
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val status =
            findViewById<TextView>(R.id.status)

        val timerView =
            findViewById<TextView>(R.id.timer)

        val focusButton =
            findViewById<Button>(R.id.focusButton)

        val resetButton =
            findViewById<Button>(R.id.resetButton)

        val chooseAppsButton =
            findViewById<Button>(R.id.chooseAppsButton)

        val selectedApps =
            findViewById<TextView>(R.id.selectedApps)

        fun refreshSelectedApps() {
            selectedApps.text =
                "Allowed apps: ${Prefs.allowed(this).size}"
        }

        fun resetUi() {
            timer?.cancel()
            timer = null
            running = false

            Prefs.setFocus(this, false)

            status.text = "Focus mode is off"
            timerView.text = "30:00"
            focusButton.text = "Start Focus"
        }

        fun startRemainingTimer() {

            timer?.cancel()
            timer = null

            val remaining =
                Prefs.remaining(this)

            if (
                remaining <= 0L ||
                !Prefs.enabled(this)
            ) {
                resetUi()
                return
            }

            running = true

            status.text =
                "Focus mode is active"

            focusButton.text =
                "Pause Focus"

            timer =
                object : CountDownTimer(
                    remaining,
                    1000L
                ) {

                    override fun onTick(
                        millisUntilFinished: Long
                    ) {

                        val totalSeconds =
                            millisUntilFinished / 1000L

                        val minutes =
                            totalSeconds / 60L

                        val seconds =
                            totalSeconds % 60L

                        timerView.text =
                            String.format(
                                "%02d:%02d",
                                minutes,
                                seconds
                            )
                    }

                    override fun onFinish() {
                        resetUi()

                        status.text =
                            "Focus session complete"
                    }
                }.start()
        }

        refreshSelectedApps()

        chooseAppsButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AppPickerActivity::class.java
                )
            )
        }

        focusButton.setOnClickListener {

            if (running) {

                resetUi()

            } else {

                val duration =
                    30L * 60L * 1000L

                val endTime =
                    System.currentTimeMillis() +
                    duration

                Prefs.setFocus(
                    this,
                    true,
                    endTime
                )

                startRemainingTimer()
            }
        }

        resetButton.setOnClickListener {
            resetUi()
        }

        /*
         * If Focus was already running before the Activity
         * was closed, restore it instead of starting 30:00.
         */
        if (Prefs.enabled(this)) {
            startRemainingTimer()
        } else {
            resetUi()
        }
    }

    override fun onResume() {
        super.onResume()

        findViewById<TextView>(
            R.id.selectedApps
        ).text =
            "Allowed apps: ${Prefs.allowed(this).size}"

        /*
         * Re-check Focus whenever the Activity becomes visible.
         */
        if (
            Prefs.enabled(this) &&
            !running
        ) {
            val timerView =
                findViewById<TextView>(R.id.timer)

            val focusButton =
                findViewById<Button>(R.id.focusButton)

            val status =
                findViewById<TextView>(R.id.status)

            val remaining =
                Prefs.remaining(this)

            if (remaining > 0L) {

                running = true

                status.text =
                    "Focus mode is active"

                focusButton.text =
                    "Pause Focus"

                timer =
                    object : CountDownTimer(
                        remaining,
                        1000L
                    ) {

                        override fun onTick(
                            millisUntilFinished: Long
                        ) {

                            val totalSeconds =
                                millisUntilFinished /
                                1000L

                            val minutes =
                                totalSeconds / 60L

                            val seconds =
                                totalSeconds % 60L

                            timerView.text =
                                String.format(
                                    "%02d:%02d",
                                    minutes,
                                    seconds
                                )
                        }

                        override fun onFinish() {
                            timer?.cancel()
                            timer = null
                            running = false

                            Prefs.setFocus(
                                this@MainActivity,
                                false
                            )

                            status.text =
                                "Focus session complete"

                            timerView.text =
                                "30:00"

                            focusButton.text =
                                "Start Focus"
                        }
                    }.start()
            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        timer = null

        super.onDestroy()
    }
}
                    override fun onFinish() {
                        resetUi()
                        status.text =
                            "Focus session complete"
                    }
                }.start()
            }
        }

        resetButton.setOnClickListener {
            resetUi()
        }
    }

    override fun onResume() {
        super.onResume()

        findViewById<TextView>(
            R.id.selectedApps
        ).text =
            "Allowed apps: ${Prefs.allowed(this).size}"
    }

    override fun onDestroy() {
        timer?.cancel()
        timer = null

        super.onDestroy()
    }
}
