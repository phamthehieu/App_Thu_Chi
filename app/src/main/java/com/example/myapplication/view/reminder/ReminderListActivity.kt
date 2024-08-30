package com.example.myapplication.view.reminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCustomizeRemindersBinding
import com.example.myapplication.viewModel.DailyReminderViewModel
import com.example.myapplication.viewModel.DailyReminderViewModelFactory

class ReminderListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomizeRemindersBinding

    private val dailyReminderViewModel: DailyReminderViewModel by viewModels{
        DailyReminderViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

    }

    private fun setupUI() {
        binding.addReminderBtn.setOnClickListener {
            startActivity(Intent(this, CustomizeRemindersActivity::class.java))
        }
    }
}