package com.example.myapplication.view.reminder

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.AccountType
import com.example.myapplication.databinding.ActivityCustomizeReminders2Binding
import com.example.myapplication.entity.DailyReminder
import com.example.myapplication.view.account.BottomSheetTypeAccountFragment
import com.example.myapplication.view.calendar.CalendarDialogFragment
import com.example.myapplication.view.component.TimePickerDialogFragment
import com.example.myapplication.viewModel.DailyReminderViewModel
import com.example.myapplication.viewModel.DailyReminderViewModelFactory
import java.time.LocalDate
import java.util.Calendar

class CustomizeRemindersActivity : AppCompatActivity(),
    TimePickerDialogFragment.OnTimeSelectedListener,
    CalendarDialogFragment.OnDateSelectedListener,
    BottomSheetTypeAccountFragment.OnAccountTypeSelectedListener {

    private lateinit var binding: ActivityCustomizeReminders2Binding

    private var nameReminderEt = "Lời nhắc nhở"
    private var frequency = "Hàng ngày"

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate = LocalDate.now()

    private val calendar: Calendar = Calendar.getInstance()

    private var currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    private var currentMinute = calendar.get(Calendar.MINUTE)

    private var note = "Đừng quên chi chép lại các khoản chi tiêu của bạn!"

    private val dailyReminderViewModel: DailyReminderViewModel by viewModels {
        DailyReminderViewModelFactory(application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeReminders2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        updateDisplayedTime(currentHour, currentMinute)

        binding.nameReminderEt.setText(nameReminderEt)
        binding.textDateTv.text =
            "${selectedDate.dayOfMonth} thg ${selectedDate.monthValue}, ${selectedDate.year}"
        binding.textFrequency.text = frequency
        binding.noteReminderEt.setText(note)

        binding.selectTimeType.setOnClickListener {
            val timePickerDialogFragment = TimePickerDialogFragment()
            timePickerDialogFragment.setTime(currentHour, currentMinute)
            timePickerDialogFragment.setOnTimeSelectedListener(this)
            timePickerDialogFragment.show(supportFragmentManager, "TimePickerDialogFragment")
        }

        binding.selectDateType.setOnClickListener {
            showCustomDialogAddCategory()
        }

        binding.selectFrequency.setOnClickListener {
            val accountTypeBottomSheet = BottomSheetTypeAccountFragment()
            val bundle = Bundle()
            bundle.putString("type", "reminder")
            accountTypeBottomSheet.arguments = bundle
            accountTypeBottomSheet.setOnReminderTypeSelectedListener(this)
            accountTypeBottomSheet.show(supportFragmentManager, accountTypeBottomSheet.tag)
        }

        binding.nameReminderEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                note = s.toString()
            }

        })

        binding.noteReminderEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                nameReminderEt = s.toString()
            }

        })

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveDataToServer() {
        val dataFormat = DailyReminder(
            name = nameReminderEt,
            frequency = frequency,
            date = selectedDate.toString(),
            hour = currentHour,
            minute = currentMinute,
            note = note
        )
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateDisplayedTime(hour: Int, minute: Int) {
        val formattedHour = String.format("%02d", hour)
        val formattedMinute = String.format("%02d", minute)
        binding.textTimeTv.text = "$formattedHour:$formattedMinute"
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        currentHour = hour
        currentMinute = minute
        calendar.set(Calendar.HOUR_OF_DAY, currentHour)
        calendar.set(Calendar.MINUTE, currentMinute)
        updateDisplayedTime(currentHour, currentMinute)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCustomDialogAddCategory() {
        val calendarDialogFragment = CalendarDialogFragment()
        val bundle = Bundle()
        bundle.putString("selectedDate", selectedDate.toString())
        bundle.putString("type", "reminder")
        calendarDialogFragment.arguments = bundle
        calendarDialogFragment.setOnDateSelectedListener(this)
        calendarDialogFragment.show(supportFragmentManager, "CalendarDialogFragment")
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceiveDate(year: String, month: String, day: String) {
        binding.textDateTv.text = "$day thg $month, $year"
        selectedDate = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
    }

    override fun onAccountTypeSelected(accountType: AccountType, position: Int) {
        frequency = accountType.name
        binding.textFrequency.text = frequency
    }

}
