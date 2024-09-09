package com.example.myapplication.view.reminder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.DailyReminderAdapter
import com.example.myapplication.databinding.ActivityCustomizeRemindersBinding
import com.example.myapplication.entity.DailyReminder
import com.example.myapplication.notification.ReminderBroadcastReceiver
import com.example.myapplication.viewModel.DailyReminderViewModel
import com.example.myapplication.viewModel.DailyReminderViewModelFactory
import java.util.Calendar

class ReminderListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomizeRemindersBinding

    private val dailyReminderViewModel: DailyReminderViewModel by viewModels{
        DailyReminderViewModelFactory(application)
    }

    companion object {
        const val REQUEST_NOTIFICATION_PERMISSION_CODE = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewReminders.layoutManager = LinearLayoutManager(this)
        dailyReminderViewModel.allDailyReminders.observe(this) { reminders ->
            Log.d("Hieu62", "setupRecyclerView: $reminders")
            if (reminders.isEmpty()) {
                binding.recyclerViewReminders.visibility = View.GONE
                binding.emptyDataView.visibility = View.VISIBLE
            } else {
                binding.recyclerViewReminders.visibility = View.VISIBLE
                binding.emptyDataView.visibility = View.GONE
                binding.recyclerViewReminders.adapter = DailyReminderAdapter(reminders){ reminder ->
                    val intent = Intent(this, CustomizeRemindersActivity::class.java)
                    intent.putExtra("REMINDER", reminder)
                    startActivity(intent)
                }
                reminders.forEach { reminder ->
                    setReminderAlarm(this, reminder)
                }
            }
        }
    }

    private fun setupUI() {
        binding.addReminderBtn.setOnClickListener {
            startActivity(Intent(this, CustomizeRemindersActivity::class.java))
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("Permission", "Notification permission granted.")
            } else {
                Log.d("Permission", "Notification permission denied.")
            }
        }
    }


    private fun checkAndRequestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm", "ObsoleteSdkInt")
    private fun setReminderAlarm(context: Context, reminder: DailyReminder) {
        checkAndRequestNotificationPermission(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("REMINDER_NAME", reminder.name)
            putExtra("REMINDER_NOTE", reminder.note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentTime = Calendar.getInstance()

        val reminderTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
        }

        if (reminderTime.before(currentTime)) {
            Log.d("Reminder", "Reminder time has passed for: ${reminder.name}")
            return
        }

        when (reminder.frequency) {
            "Hàng ngày" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            "Hàng tuần" -> {
                if (reminderTime.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime.timeInMillis,
                        pendingIntent
                    )
                }
            }
            "Hàng tháng" -> {
                if (reminderTime.get(Calendar.DAY_OF_MONTH) == 1) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime.timeInMillis,
                        pendingIntent
                    )
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }


}