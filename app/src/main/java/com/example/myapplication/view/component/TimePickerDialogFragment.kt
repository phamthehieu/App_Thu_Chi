package com.example.myapplication.view.component

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTimePickerDialogBinding

class TimePickerDialogFragment : DialogFragment() {

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var tvTimeDisplay: TextView

    private var listener: OnTimeSelectedListener? = null

    // Bước 1: Thêm biến để lưu giờ và phút
    private var hour: Int = 0
    private var minute: Int = 0

    interface OnTimeSelectedListener {
        fun onTimeSelected(hour: Int, minute: Int)
    }

    fun setOnTimeSelectedListener(listener: OnTimeSelectedListener) {
        this.listener = listener
    }

    fun setTime(hour: Int, minute: Int) {
        Log.d("Hieu38", "Setting time to $hour:$minute")
        this.hour = hour
        this.minute = minute
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_time_picker_dialog, container, false)

        hourPicker = view.findViewById(R.id.hourPicker)
        minutePicker = view.findViewById(R.id.minutePicker)
        tvTimeDisplay = view.findViewById(R.id.tvTimeDisplay)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        minutePicker.minValue = 0
        minutePicker.maxValue = 59

        hourPicker.value = hour
        minutePicker.value = minute

        updateTimeDisplay(hour, minute)

        hourPicker.setOnValueChangedListener { _, _, newVal ->
            updateTimeDisplay(newVal, minutePicker.value)
        }

        minutePicker.setOnValueChangedListener { _, _, newVal ->
            updateTimeDisplay(hourPicker.value, newVal)
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val selectedHour = hourPicker.value
            val selectedMinute = minutePicker.value

            listener?.onTimeSelected(selectedHour, selectedMinute)
            dismiss()
        }

        return view
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimeDisplay(hour: Int, minute: Int) {
        val formattedTime = String.format("%02d:%02d", hour, minute)
        tvTimeDisplay.text = formattedTime
    }
}

