package com.example.myapplication.view.component

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.L
import com.example.myapplication.R
import com.example.myapplication.databinding.Example5CalendarDayBinding
import com.example.myapplication.databinding.Example5CalendarHeaderBinding
import com.example.myapplication.databinding.Example5EventItemViewBinding
import com.example.myapplication.databinding.FragmentCalendarHomeBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class Example5FlightsAdapter :
    RecyclerView.Adapter<Example5FlightsAdapter.Example5FlightsViewHolder>() {
    val flights = mutableListOf<IncomeExpenseList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Example5FlightsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = Example5EventItemViewBinding.inflate(inflater, parent, false)
        return Example5FlightsViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: Example5FlightsViewHolder, position: Int) {
        viewHolder.bind(flights[position])
    }

    override fun getItemCount(): Int = flights.size

    inner class Example5FlightsViewHolder(val binding: Example5EventItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(data: IncomeExpenseList) {
            binding.iconCategoryCalendarIV.setImageResource(data.iconResource)
            binding.iconCategoryCalendarIV.setColorFilter(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.black
                )
            )
            val drawable =
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.setting_background_item
                ) as GradientDrawable
            drawable.setColor(generateRandomLightColor())

            val amount = data.amount.replace(",", ".").toDouble()
            val expenseFormatter = DecimalFormat("#,###.##")
            val formattedAmount = expenseFormatter.format(amount)

            if (data.type == "Expense") {
                binding.amountCategoryCalendarTV.text = "-$formattedAmount"
            } else {
                binding.amountCategoryCalendarTV.text = formattedAmount
            }
            binding.nameCategoryCalendarTV.text = data.categoryName
        }
    }

    private fun generateRandomLightColor(): Int {
        val random = java.util.Random()
        val baseColor =
            Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        val lightColor = blendWithWhite(baseColor, 0.5f)
        return if (lightColor == Color.WHITE || lightColor == Color.BLACK) generateRandomLightColor() else lightColor
    }

    private fun blendWithWhite(color: Int, ratio: Float): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        val blendedRed = (red + (255 - red) * ratio).toInt()
        val blendedGreen = (green + (255 - green) * ratio).toInt()
        val blendedBlue = (blue + (255 - blue) * ratio).toInt()

        return Color.argb(255, blendedRed, blendedGreen, blendedBlue)
    }
}

class CalendarHomeActivity : AppCompatActivity() {

    private var selectedDate: LocalDate? = null

    private val flightsAdapter = Example5FlightsAdapter()

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    private lateinit var binding: FragmentCalendarHomeBinding

    private val incomeExpenseList = mutableListOf<IncomeExpenseList>()
    private val incomeExpensesByDate = mutableMapOf<LocalDate, List<IncomeExpenseList>>()

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCalendarHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeExpenseListModel.allIncomeExpense.observe(this) { list ->
            incomeExpenseList.clear()
            incomeExpenseList.addAll(list)
            incomeExpensesByDate.clear()
            incomeExpensesByDate.putAll(incomeExpenseList.groupBy { LocalDate.parse(it.date) })
            updateAdapterForDate(selectedDate)
        }

        binding.exFiveRv.apply {
            layoutManager =
                LinearLayoutManager(this@CalendarHomeActivity, RecyclerView.VERTICAL, false)
            adapter = flightsAdapter
        }
        flightsAdapter.notifyDataSetChanged()

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        configureBinders(daysOfWeek)
        binding.exFiveCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.exFiveCalendar.scrollToMonth(currentMonth)

        binding.exFiveCalendar.monthScrollListener = { month ->
            binding.exFiveMonthYearText.text = month.yearMonth.month.name.capitalize(Locale.ENGLISH)

            selectedDate?.let {
                selectedDate = null
                binding.exFiveCalendar.notifyDateChanged(it)
                updateAdapterForDate(null)
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
//        flightsAdapter.flights.clear()
//        flightsAdapter.flights.addAll(incomeExpensesByDate[date].orEmpty())
//        flightsAdapter.notifyDataSetChanged()
        Log.d("Hieu190", "${incomeExpensesByDate[date]}")
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
                    if (data != null && data.isNotEmpty()) {
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

                        flightTopView.text =  expenseFormatter.format(totalIncome)
                        flightTopView.setTextColor(context.getColor(R.color.green_text))
                        flightTopView.visibility = if (totalIncome > 0.0) View.VISIBLE else View.GONE

                        flightBottomView.text = expenseFormatter.format(totalExpense)
                        flightBottomView.setTextColor(context.getColor(R.color.red))
                        flightBottomView.visibility = if (totalExpense > 0.0) View.VISIBLE else View.GONE

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
