package com.example.myapplication.view.calendar

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.myapplication.R
import com.example.myapplication.databinding.Example5CalendarDayBinding
import com.example.myapplication.databinding.Example5CalendarHeaderBinding
import com.example.myapplication.databinding.FragmentCalendarHomeBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.view.revenue_and_expenditure.RevenueAndExpenditureActivity
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.google.gson.Gson
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class CalendarHomeActivity : AppCompatActivity() {

    private var selectedDate: LocalDate? = null
    private var isDateSelected: Boolean = false

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    private lateinit var binding: FragmentCalendarHomeBinding

    private val incomeExpenseList = mutableListOf<IncomeExpenseList>()
    private val incomeExpensesByDate = mutableMapOf<LocalDate, List<IncomeExpenseList>>()

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCalendarHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeButton.setOnClickListener {
            finish()
        }

        incomeExpenseListModel.allIncomeExpense.observe(this) { list ->
            incomeExpenseList.clear()
            incomeExpenseList.addAll(list)
            incomeExpensesByDate.clear()
            incomeExpensesByDate.putAll(incomeExpenseList.groupBy { LocalDate.parse(it.date) })
            if (isDateSelected) { // Chỉ cập nhật adapter khi người dùng đã chọn ngày
                updateAdapterForDate(selectedDate)
            }
        }

        binding.fabBtn.setColorFilter(R.color.black)

        binding.fabBtn.setOnClickListener {
            startActivity(Intent(this, RevenueAndExpenditureActivity::class.java))
        }

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        configureBinders(daysOfWeek)
        binding.exFiveCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.exFiveCalendar.scrollToMonth(currentMonth)

        binding.exFiveCalendar.monthScrollListener = { month ->

            val yearMonth = month.yearMonth
            val monthNumber = yearMonth.monthValue
            val year = yearMonth.year

            binding.exFiveMonthYearText.text = "thg $monthNumber $year"

            selectedDate?.let {
                selectedDate = null
                binding.exFiveCalendar.notifyDateChanged(it)
            }
        }

        binding.exFiveNextMonthImage.setOnClickListener {
            binding.exFiveCalendar.findFirstVisibleMonth()?.let {
                binding.exFiveCalendar.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }

        binding.exFivePreviousMonthImage.setOnClickListener {
            binding.exFiveCalendar.findFirstVisibleMonth()?.let {
                binding.exFiveCalendar.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAdapterForDate(date: LocalDate?) {
        val gson = Gson()
        val json = gson.toJson(incomeExpensesByDate[date])
        val intent = Intent(this, CategoryListCalendarActivity::class.java)
        intent.putExtra("listCategory", json)

        if (date != null) {
            intent.putExtra("dateSelected", date.toString())
        }

        startActivity(intent)
    }


    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        @RequiresApi(Build.VERSION_CODES.O)
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = Example5CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            isDateSelected = true // Đánh dấu rằng người dùng đã chọn ngày
                            val binding = this@CalendarHomeActivity.binding
                            binding.exFiveCalendar.notifyDateChanged(day.date)
                            oldDate?.let { binding.exFiveCalendar.notifyDateChanged(it) }
                            updateAdapterForDate(day.date)
                        }
                    }
                }
            }
        }

        binding.exFiveCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun create(view: View) = DayViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val context = container.binding.root.context
                val textView = container.binding.exFiveDayText
                val layout = container.binding.exFiveDayLayout
                textView.text = data.date.dayOfMonth.toString()

                val flightTopView = container.binding.exFiveDayFlightTop
                val flightBottomView = container.binding.exFiveDayFlightBottom
                flightTopView.background = null
                flightBottomView.background = null

                if (data.position == DayPosition.MonthDate) {
                    textView.setTextColor(context.getColor(R.color.example_5_text_grey))
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.example_5_selected_bg else 0)
                    val data = incomeExpensesByDate[data.date]
                    if (!data.isNullOrEmpty()) {
                        var totalIncome = 0.0
                        var totalExpense = 0.0
                        for (item in data) {
                            val amount = parseAmount(item.amount)
                            if (item.type == "Income") {
                                totalIncome += amount
                            } else if (item.type == "Expense") {
                                totalExpense += amount
                            }
                        }

                        val expenseFormatter = DecimalFormat("#,###.##")

                        flightTopView.text = expenseFormatter.format(totalIncome)
                        flightTopView.setTextColor(context.getColor(R.color.green_text))
                        flightTopView.visibility =
                            if (totalIncome > 0.0) View.VISIBLE else View.GONE

                        flightBottomView.text = expenseFormatter.format(totalExpense)
                        flightBottomView.setTextColor(context.getColor(R.color.red))
                        flightBottomView.visibility =
                            if (totalExpense > 0.0) View.VISIBLE else View.GONE

                        if (totalExpense > 0.0 || totalIncome > 0.0) {
                            container.binding.all.setBackgroundResource(R.drawable.rounded_background_calendar_day_selected)
                        } else {
                            container.binding.all.setBackgroundResource(R.drawable.rounded_background_calendar_day)
                        }
                    } else {
                        flightTopView.visibility = View.GONE
                        flightBottomView.visibility = View.GONE
                        container.binding.all.setBackgroundResource(R.drawable.rounded_background_calendar_day)
                    }

                } else {
                    container.binding.all.visibility = View.GONE
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = Example5CalendarHeaderBinding.bind(view).legendLayout.root
        }

        val typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL)
        binding.exFiveCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)

                @RequiresApi(Build.VERSION_CODES.O)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                                )
                                tv.setTextColor(
                                    ContextCompat.getColor(
                                        this@CalendarHomeActivity,
                                        R.color.white
                                    )
                                )
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }

    private fun parseAmount(amountStr: String): Double {
        val cleanedAmountStr = amountStr.replace(".", "").replace(",", ".")
        return try {
            cleanedAmountStr.toDouble()
        } catch (e: NumberFormatException) {
            Log.e("IncomeExpenseListAdapter", "Error parsing amount: $amountStr", e)
            0.0
        }
    }
}
