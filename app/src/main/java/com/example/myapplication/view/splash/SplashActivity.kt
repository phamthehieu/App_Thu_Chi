package com.example.myapplication.view.splash

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.entity.Account
import com.example.myapplication.utilities.ThemeUtils
import com.example.myapplication.view.aggregatedMonthly.AggregatedMonthlyMainActivity
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import java.util.Calendar


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
        ThemeUtils.checkCurrentTheme(this)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeUtils.checkCurrentTheme(this)
    }

    private fun setup() {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val savedDate = sharedPreferences.getInt("selectedDate", 1)
        val checkDate = sharedPreferences.getBoolean("checkDate", false)
        val currentDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        val lottieAnimationView = binding.lottieAnimationView
        lottieAnimationView.setAnimation("23131.json")
        lottieAnimationView.speed = 2.0f
        lottieAnimationView.playAnimation()
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                if (savedDate == currentDate && !checkDate) {
                    val intent = Intent(this@SplashActivity, AggregatedMonthlyMainActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
}