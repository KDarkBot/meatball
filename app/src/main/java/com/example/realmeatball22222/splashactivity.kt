package com.example.realmeatball22222

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.BounceInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.realmeatball22222.databinding.ActivitySplashactivityBinding

class splashactivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivitySplashactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val meetball = binding.meetballlogo
        val meetballplate = binding.meetballplate

        meetballplate.post {
            try {
                val plateY = meetballplate.top.toFloat() - 200
                val ballY = meetball.top.toFloat()
                val distance = plateY - ballY

                val transAnim = TranslateAnimation(
                    0f, 0f, 0f, distance
                ).apply {
                    startOffset = 500
                    duration = 3000
                    fillAfter = true
                    interpolator = BounceInterpolator()
                }

                transAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            val shakeAnim = TranslateAnimation(-15f, 15f, 0f, 50f).apply {
                                duration = 1000
                                repeatCount = 10
                                repeatMode = Animation.REVERSE
                                fillAfter = true
                            }
                            meetballplate.startAnimation(shakeAnim)

                            val rotateAnim = RotateAnimation(
                                -15f, 15f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                            ).apply {
                                startOffset = 100
                                duration = 700
                                repeatCount = 1
                                repeatMode = Animation.REVERSE
                                fillAfter = true
                            }

                            val bounceAnim = TranslateAnimation(
                                0f, 0f, 0f, -100f
                            ).apply {
                                duration = 500
                                repeatMode = Animation.REVERSE
                                repeatCount = 1
                                fillAfter = true
                            }

                            val animationSet = AnimationSet(true).apply {
                                addAnimation(rotateAnim)
                                addAnimation(bounceAnim)
                            }

                            animationSet.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {}

                                override fun onAnimationEnd(animation: Animation?) {
                                    val resetRotationAnim = RotateAnimation(
                                        -15f, 0f,
                                        Animation.RELATIVE_TO_SELF, 0.5f,
                                        Animation.RELATIVE_TO_SELF, 0.5f
                                    ).apply {
                                        duration = 800
                                        fillAfter = true
                                    }
                                    meetballplate.startAnimation(resetRotationAnim)
                                }

                                override fun onAnimationRepeat(animation: Animation?) {}
                            })

                            meetballplate.startAnimation(animationSet)
                        }, 1500)
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            // 애니메이션 종료 후 MainActivity로 이동
                            val intent = Intent(this@splashactivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Splash 액티비티를 종료
                        }, 1000)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })

                meetball.startAnimation(transAnim)
            } catch (e: Exception) {
                Log.e("Splash", "Error during animation setup", e)
            }
        }
    }
}
