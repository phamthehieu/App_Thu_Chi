package com.example.myapplication.view.calendar

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.metrics.Event
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.myapplication.adapter.YearAdapter
import com.example.myapplication.databinding.ActivityCalendarBinding
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import com.example.myapplication.R
import com.example.myapplication.databinding.Example3CalendarDayBinding
import com.example.myapplication.databinding.Example3CalendarHeaderBinding
import com.example.myapplication.view.component.KeyBoardBottomSheetFragment

@SuppressLint("RestrictedApi")
class CalendarDialogFragment : DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private var today = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("yyyy")
    private val events = mutableMapOf<LocalDate, List<Event>>()

    private var isYearPickerVisible = false

    private var yearSelected = ""
    private var monthSelected = ""
    private var dayOfMonthSelected = ""
    private var dayOfWeekSelected = ""

    private lateinit var binding: ActivityCalendarBinding

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        AndroidThreeTen.init(requireContext())
        binding = ActivityCalendarBinding.inflate(inflater, container, false)
        arguments?.let {
            val dateString = it.getString("selectedDate")
            today = LocalDate.parse(dateString)
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireDialog().window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.yellow)

        binding.exThreeCalendar.monthScrollListener = { calendarMonth ->
            val firstDayOfMonth = LocalDate.of(
                calendarMonth.yearMonth.year,
                calendarMonth.yearMonth.month.value,
                1
            )
            (activity as? AppCompatActivity)?.supportActionBar?.title = titleSameYearFormatter.format(firstDayOfMonth)
            selectDate(firstDayOfMonth)
        }

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(50)

        configureBinders(daysOfWeek)
        binding.exThreeCalendar.apply {
            setup(startMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        if (savedInstanceState == null) {
            binding.exThreeCalendar.post { selectDate(today) }
        }

        binding.exThreeBtnPrevious.setOnClickListener {
            showPreviousMonth()
        }

        binding.exThreeBtnNext.setOnClickListener {
            showNextMonth()
        }

        binding.textYearTitle.setOnClickListener {
            toggleYearPicker()
        }

        binding.backBtn.setOnClickListener {
            dismiss()
        }

        binding.successBtn.setOnClickListener {
            val targetFragment = targetFragment as? KeyBoardBottomSheetFragment
            targetFragment?.onReceiveDate(yearSelected, monthSelected, dayOfMonthSelected)
            dismiss()
        }

        val years = (1999..2099).map { it.toString() }
        val adapter = YearAdapter(requireContext(), years, yearSelected)
        val gridView = binding.root.findViewById<GridView>(R.id.yearGridView)
        gridView.adapter = adapter

        val currentYearIndex = years.indexOf(LocalDate.now().year.toString())
        gridView.setSelection(currentYearIndex)

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedYear = years[position]
            yearSelected = selectedYear
            binding.textYearTitle.text = "tháng $monthSelected, năm $selectedYear"
            binding.exThreeSelectedDateText.text = "$dayOfWeekSelected, $dayOfMonthSelected thg $monthSelected, $selectedYear"
            (gridView.adapter as YearAdapter).setSelectedYear(selectedYear)
            (gridView.adapter as YearAdapter).notifyDataSetChanged()
        }
    }

    private fun toggleYearPicker() {
        if (isYearPickerVisible) {
            switchToCalendarView()
        } else {
            showYearPicker()
        }
        isYearPickerVisible = !isYearPickerVisible
    }

    private fun showYearPicker() {
        val calendarViewLayout = binding.root.findViewById<RelativeLayout>(R.id.calendarViewLayout)
        val yearPickerLayout = binding.root.findViewById<RelativeLayout>(R.id.yearPickerLayout)
        val exThreeBtnPrevious = binding.root.findViewById<ImageButton>(R.id.exThreeBtnPrevious)
        val exThreeBtnNext = binding.root.findViewById<ImageButton>(R.id.exThreeBtnNext)

        exThreeBtnPrevious.visibility = View.GONE
        exThreeBtnNext.visibility = View.GONE
        calendarViewLayout.visibility = View.GONE
        yearPickerLayout.visibility = View.VISIBLE
    }

    private fun switchToCalendarView() {
        val calendarViewLayout = binding.root.findViewById<RelativeLayout>(R.id.calendarViewLayout)
        val yearPickerLayout = binding.root.findViewById<RelativeLayout>(R.id.yearPickerLayout)
        val exThreeBtnPrevious = binding.root.findViewById<ImageButton>(R.id.exThreeBtnPrevious)
        val exThreeBtnNext = binding.root.findViewById<ImageButton>(R.id.exThreeBtnNext)

        exThreeBtnPrevious.visibility = View.VISIBLE
        exThreeBtnNext.visibility = View.VISIBLE
        yearPickerLayout.visibility = View.GONE
        calendarViewLayout.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPreviousMonth() {
        binding.exThreeCalendar.findFirstVisibleMonth()?.let { firstVisibleMonth ->
            val previousMonth = firstVisibleMonth.yearMonth.minusMonths(1)
            binding.exThreeCalendar.scrollToMonth(previousMonth)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNextMonth() {
        binding.exThreeCalendar.findLastVisibleMonth()?.let { lastVisibleMonth ->
            val nextMonth = lastVisibleMonth.yearMonth.plusMonths(1)
            binding.exThreeCalendar.scrollToMonth(nextMonth)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.exThreeCalendar.notifyDateChanged(it) }
            binding.exThreeCalendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapterForDate(date: LocalDate) {
        dayOfWeekSelected = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("vi", "VN")).replace("T", "Th").replace(".", " ")
        dayOfMonthSelected = date.dayOfMonth.toString()
        monthSelected = "${date.monthValue}"
        yearSelected = "${date.year}"
        val finalFormattedDate = "$dayOfWeekSelected, $dayOfMonthSelected thg $monthSelected, $yearSelected"
        val finalFormattedMonth = "tháng $monthSelected, năm $yearSelected"
        binding.textYearTitle.text = finalFormattedMonth
        binding.exThreeSelectedDateText.text = finalFormattedDate
    }

    private fun configureBinders(daysOfWeek: List<java.time.DayOfWeek>) {
        @RequiresApi(Build.VERSION_CODES.O)
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = Example3CalendarDayBinding.bind(view)
            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                    }
                }
            }
        }

        binding.exThreeCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun create(view: View) = DayViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.O)
            @SuppressLint("ResourceAsColor")
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.exThreeDayText
                val dotView = container.binding.exThreeDotView

                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.visibility = View.VISIBLE
                    when (data.date) {
                        selectedDate -> {
                            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            textView.setBackgroundResource(R.drawable.example_3_selected_bg)
                            dotView.visibility = View.VISIBLE
                        }

                        else -> {
                            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            textView.background = null
                            dotView.isVisible = events[data.date].orEmpty().isNotEmpty()
                        }
                    }
                } else {
                    textView.visibility = View.GONE
                    dotView.visibility = View.GONE
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = Example3CalendarHeaderBinding.bind(view).legendLayout.root
        }

        binding.exThreeCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = data.yearMonth
                    container.legendLayout.children.map { it as TextView }
                        .forEachIndexed { index, tv ->
                            tv.text = daysOfWeek[index].name.substring(0, 3)
                            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        }
                }
            }
        }
    }
}
