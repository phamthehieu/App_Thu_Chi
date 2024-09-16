package com.example.myapplication.view.reminder

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AccountType
import com.example.myapplication.databinding.ActivityCustomizeReminders2Binding
import com.example.myapplication.entity.DailyReminder
import com.example.myapplication.view.account.BottomSheetTypeAccountFragment
import com.example.myapplication.view.calendar.CalendarDialogFragment
import com.example.myapplication.view.component.TimePickerDialogFragment
import com.example.myapplication.viewModel.DailyReminderViewModel
import com.example.myapplication.viewModel.DailyReminderViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar

class CustomizeRemindersActivity : AppCompatActivity(),
    TimePickerDialogFragment.OnTimeSelectedListener,
    CalendarDialogFragment.OnDateSelectedListener,
    BottomSheetTypeAccountFragment.OnAccountTypeSelectedListener {

    private lateinit var binding: ActivityCustomizeReminders2Binding

    private var nameReminderEt = "Lời nhắc nhở"
    private var frequency = "Hàng ngày"

    private var selectedDate = LocalDate.now()

    private val calendar: Calendar = Calendar.getInstance()

    private var currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    private var currentMinute = calendar.get(Calendar.MINUTE)

    private var itemEdit: DailyReminder? = null

    private var note = "Đừng quên chi chép lại các khoản chi tiêu của bạn!"

    private val dailyReminderViewModel: DailyReminderViewModel by viewModels {
        DailyReminderViewModelFactory(application)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeReminders2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val reminder = intent.getParcelableExtra<DailyReminder>("REMINDER")
        reminder?.let {
            itemEdit = it

            calendar.set(Calendar.YEAR, it.date.substring(0, 4).toInt())
            calendar.set(Calendar.MONTH, it.date.substring(5, 7).toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, it.date.substring(8, 10).toInt())
            calendar.set(Calendar.HOUR_OF_DAY, it.hour)
            calendar.set(Calendar.MINUTE, it.minute)

            currentHour = it.hour
            currentMinute = it.minute
            selectedDate = LocalDate.of(
                it.date.substring(0, 4).toInt(),
                it.date.substring(5, 7).toInt(),
                it.date.substring(8, 10).toInt()
            )

            updateDisplayedTime(currentHour, currentMinute)
            binding.nameReminderEt.setText(it.name)
            binding.textDateTv.text = "${selectedDate.dayOfMonth} thg ${selectedDate.monthValue}, ${selectedDate.year}"
            binding.textFrequency.text = it.frequency
            binding.noteReminderEt.setText(it.note)
            binding.deleteReminderBtn.visibility = View.VISIBLE
        } ?: run {
            updateDisplayedTime(currentHour, currentMinute)
            binding.nameReminderEt.setText(nameReminderEt)
            binding.textDateTv.text = "${selectedDate.dayOfMonth} thg ${selectedDate.monthValue}, ${selectedDate.year}"
            binding.textFrequency.text = frequency
            binding.noteReminderEt.setText(note)
            binding.deleteReminderBtn.visibility = View.GONE
        }


        setupUI()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setupUI() {
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
                nameReminderEt = s.toString()
            }

        })

        binding.noteReminderEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                note = s.toString()
            }

        })

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addNewAccountBtn.setOnClickListener {
            saveDataToServer()
        }

        binding.deleteReminderBtn.setOnClickListener {
            itemEdit?.let {
                dailyReminderViewModel.viewModelScope.launch(Dispatchers.IO) {
                    dailyReminderViewModel.deleteDailyReminder(it)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CustomizeRemindersActivity, "Xóa lời nhắc thành công", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun saveDataToServer() {
       if (itemEdit === null) {
           val dataFormat = DailyReminder(
               name = nameReminderEt,
               frequency = frequency,
               date = selectedDate.toString(),
               hour = currentHour,
               minute = currentMinute,
               note = note
           )

           dailyReminderViewModel.viewModelScope.launch(Dispatchers.IO) {
               val insertedId = dailyReminderViewModel.insertDailyReminder(dataFormat)

               withContext(Dispatchers.Main) {
                   if (insertedId.toString() == "kotlin.Unit") {
                       Toast.makeText(this@CustomizeRemindersActivity, "Thêm lời nhắc thành công", Toast.LENGTH_SHORT).show()
                       finish()
                   }
               }
           }
       } else {
           val dataFormat = DailyReminder(
               id = itemEdit!!.id,
               name = nameReminderEt,
               frequency = frequency,
               date = selectedDate.toString(),
               hour = currentHour,
               minute = currentMinute,
               note = note
           )
           dailyReminderViewModel.viewModelScope.launch(Dispatchers.IO) {
               dailyReminderViewModel.updateDailyReminder(dataFormat)
               withContext(Dispatchers.Main) {
                   Toast.makeText(this@CustomizeRemindersActivity, "Cập nhật lời nhắc thành công", Toast.LENGTH_SHORT).show()
                   finish()
               }
           }
       }
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
    override fun onReceiveDate(year: String, month: String, day: String) {
        binding.textDateTv.text = "$day thg $month, $year"
        selectedDate = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
    }

    override fun onAccountTypeSelected(accountType: AccountType, position: Int) {
        frequency = accountType.name
        binding.textFrequency.text = frequency
    }

}
