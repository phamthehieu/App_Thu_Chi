package com.example.myapplication.view.aggregatedMonthly

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityAggregatedMonthlyMainBinding

class AggregatedMonthlyMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAggregatedMonthlyMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAggregatedMonthlyMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}