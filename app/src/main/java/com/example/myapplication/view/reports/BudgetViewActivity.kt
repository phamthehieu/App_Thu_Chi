package com.example.myapplication.view.reports

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityBudgetViewBinding

class BudgetViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addNewBudget.setOnClickListener {
            val settingBudgetIntent = Intent(this, SettingBudgetActivity::class.java)
            startActivity(settingBudgetIntent)
        }
    }
}