package com.example.myapplication.view.chart

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.DataListChartAdapter
import com.example.myapplication.adapter.MonthPagerAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityDetailedChartOfCategoryBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.example.myapplication.utilities.convertToIncomeExpenseListData
import com.example.myapplication.view.home.DetailActivity
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.DecimalFormat
import java.util.Calendar

class DetailedChartOfCategoryActivity : AppCompatActivity(),
    DataListChartAdapter.OnCategoryClickListener {

    private lateinit var binding: ActivityDetailedChartOfCategoryBinding
    private lateinit var lineChart: LineChart

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var categoryData: List<CategoryWithIncomeExpenseList>? = null

    private var dateAmountList: List<Pair<Int, String>>? = null

    private var categoryId: Int? = null

    private var check = true

    private var checkMode = true

    private var checkSearch: Int = 1

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedChartOfCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lineChart = binding.lineChart

        categoryId = intent.getIntExtra("categoryId", 0)
        val categoryName = intent.getStringExtra("categoryName")

        yearSearch = intent.getIntExtra("yearSearch", Calendar.getInstance().get(Calendar.YEAR))

        monthSearch = intent.getIntExtra("monthSearch", Calendar.getInstance().get(Calendar.MONTH) + 1)

        checkSearch = intent.getIntExtra("checkSearchData", 1)

        binding.titleText.text = categoryName

        if (checkSearch == 1) {
            binding.monthTv.text = "Thg $monthSearch $yearSearch"
        } else {
            binding.monthTv.text = "Năm $yearSearch"
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.popupCalenderBtn.setOnClickListener {
            if (checkSearch == 1) {
                showCustomDialogBox()
            } else {
                showCustomDialogYear()
            }

        }

        getDataSource(categoryId!!)

        setupNightMode()

    }

    private fun setupNightMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            checkMode = true
            binding.title.setBackgroundColor(resources.getColor(R.color.grayHeader, theme))
            val color = ContextCompat.getColor(this, R.color.grayHeader)
            this.window.statusBarColor = color
            binding.calendarIcon.setColorFilter(resources.getColor(R.color.white, theme))
        } else {
            checkMode = false
            binding.title.setBackgroundColor(resources.getColor(R.color.yellow, theme))
            val color = ContextCompat.getColor(this, R.color.yellow)
            this.window.statusBarColor = color
            binding.calendarIcon.setColorFilter(resources.getColor(R.color.black, theme))
            binding.backBtn.setColorFilter(resources.getColor(R.color.black, theme))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialogYear() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 50..currentYear + 50).toList()
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.fragment_pop_up_calender)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backBtn: TextView = dialog.findViewById(R.id.backBtn)
        val textYearTitle: TextView = dialog.findViewById(R.id.textYearTitle)
        val titleRecord: TextView = dialog.findViewById(R.id.titleRecord)
        val successBtn: TextView = dialog.findViewById(R.id.successBtn)
        val yearTv: LinearLayout = dialog.findViewById(R.id.yearTv)

        fun updateTitleRecord(year: Int) {
            titleRecord.text = "năm $year"
            textYearTitle.text = year.toString()
        }
        updateTitleRecord(yearSearch)

        yearTv.visibility = View.GONE

        val viewPager: ViewPager2 = dialog.findViewById(R.id.viewPager)
        val monthPagerAdapter = MonthPagerAdapter(
            this,
            years,
            true,
            yearSearch,
            monthSearch - 1,
            object : OnMonthSelectedListener {
                override fun onMonthSelected(month: Int) {
                    yearSearch = month
                    updateTitleRecord(yearSearch)
                }
            }
        )

        viewPager.adapter = monthPagerAdapter
        viewPager.offscreenPageLimit = 1
        viewPager.isUserInputEnabled = false

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position != 0) {
                    yearSearch = years[position]
                    textYearTitle.text = years[position].toString()
                    updateTitleRecord(yearSearch)
                    monthPagerAdapter.updateYearData(yearSearch)
                }
            }
        })

        backBtn.setOnClickListener {
            dialog.dismiss()
        }
        successBtn.setOnClickListener {
            binding.monthTv.text = "Năm $yearSearch"
            getDataSource(categoryId!!)
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialogBox() {
        val months = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 50..currentYear + 50).toList()
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.fragment_pop_up_calender)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backBtn: TextView = dialog.findViewById(R.id.backBtn)
        val yearTv: LinearLayout = dialog.findViewById(R.id.yearTv)
        val yearIcon: ImageButton = dialog.findViewById(R.id.yearIcon)
        val textYearTitle: TextView = dialog.findViewById(R.id.textYearTitle)
        val titleRecord: TextView = dialog.findViewById(R.id.titleRecord)
        val leftBtn: ImageButton = dialog.findViewById(R.id.leftBtn)
        val rightBtn: ImageButton = dialog.findViewById(R.id.rightBtn)
        val successBtn: TextView = dialog.findViewById(R.id.successBtn)

        if (checkMode) {
            leftBtn.setColorFilter(resources.getColor(R.color.white, theme))
            rightBtn.setColorFilter(resources.getColor(R.color.white, theme))
        } else {
            leftBtn.setColorFilter(resources.getColor(R.color.black, theme))
            rightBtn.setColorFilter(resources.getColor(R.color.black, theme))
        }

        fun updateTitleRecord(month: Int, year: Int) {
            titleRecord.text = "tháng $month năm $year"
            textYearTitle.text = year.toString()
        }
        updateTitleRecord(monthSearch, yearSearch)

        val viewPager: ViewPager2 = dialog.findViewById(R.id.viewPager)
        var monthPagerAdapter = MonthPagerAdapter(
            this,
            months,
            false,
            yearSearch,
            monthSearch - 1,
            object : OnMonthSelectedListener {
                override fun onMonthSelected(month: Int) {
                    if (!check) {
                        yearSearch = month
                    } else {
                        monthSearch = month + 1
                    }
                    updateTitleRecord(monthSearch, yearSearch)
                }
            }
        )
        viewPager.adapter = monthPagerAdapter
        viewPager.currentItem = 50
        viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        viewPager.currentItem = years.indexOf(currentYear)
        yearIcon.setBackgroundResource(R.drawable.ic_sort_down_24)

        leftBtn.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        rightBtn.setOnClickListener {
            if (viewPager.currentItem < (viewPager.adapter?.itemCount ?: (0 - 1))) {
                viewPager.currentItem += 1
            }
        }

        yearTv.setOnClickListener {
            if (check) {
                monthPagerAdapter = MonthPagerAdapter(
                    this,
                    years,
                    true,
                    yearSearch,
                    monthSearch - 1,
                    object : OnMonthSelectedListener {
                        override fun onMonthSelected(month: Int) {
                            if (!check) {
                                yearSearch = month
                            } else {
                                monthSearch = month + 1
                            }
                            updateTitleRecord(monthSearch, yearSearch)
                        }
                    }
                )
                leftBtn.visibility = View.GONE
                rightBtn.visibility = View.GONE
                viewPager.adapter = monthPagerAdapter
                viewPager.offscreenPageLimit = 1
                viewPager.isUserInputEnabled = false
                yearIcon.setBackgroundResource(R.drawable.ic_sort_up_24)
                check = false
            } else {
                monthPagerAdapter = MonthPagerAdapter(
                    this,
                    months,
                    false,
                    yearSearch,
                    monthSearch - 1,
                    object : OnMonthSelectedListener {
                        override fun onMonthSelected(month: Int) {
                            if (!check) {
                                yearSearch = month
                            } else {
                                monthSearch = month + 1
                            }
                            updateTitleRecord(monthSearch, yearSearch)
                        }
                    }
                )
                leftBtn.visibility = View.VISIBLE
                rightBtn.visibility = View.VISIBLE
                viewPager.adapter = monthPagerAdapter
                viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                viewPager.isUserInputEnabled = true
                yearIcon.setBackgroundResource(R.drawable.ic_sort_down_24)
                check = true
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position != 0) {
                    yearSearch = years[position]
                    textYearTitle.text = years[position].toString()
                    updateTitleRecord(monthSearch, yearSearch)
                    monthPagerAdapter.updateYearData(yearSearch)
                }
            }
        })

        backBtn.setOnClickListener {
            dialog.dismiss()
        }
        successBtn.setOnClickListener {
            binding.monthTv.text = "Thg $monthSearch $yearSearch"
            getDataSource(categoryId!!)
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun getDataSource(categoryId: Int) {
        Log.d("checkSearch", checkSearch.toString())
        if (checkSearch == 1) {
            val formattedMonth = String.format("%02d", monthSearch)
            incomeExpenseListModel.getIncomeExpenseListByMonthYearIdCategory(
                yearSearch.toString(),
                formattedMonth,
                categoryId
            ).observe(this) { data ->
                categoryData = data.reversed().map { item ->
                    CategoryWithIncomeExpenseList(
                        incomeExpense = IncomeExpenseList(
                            id = item.incomeExpense.id,
                            note = item.incomeExpense.note,
                            amount = item.incomeExpense.amount,
                            date = item.incomeExpense.date,
                            categoryId = item.incomeExpense.categoryId,
                            type = item.incomeExpense.type,
                            image = item.incomeExpense.image,
                            categoryName = item.incomeExpense.categoryName,
                            iconResource = item.incomeExpense.iconResource,
                            accountId = item.incomeExpense.accountId
                        ),
                        category = item.category
                    )
                }

                val dataSetup = data.map { convertToIncomeExpenseListData(it) }

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                dateAmountList = dataSetup.map {
                    val date = LocalDate.parse(it.date, formatter)
                    val dayOfMonth = date.dayOfMonth
                    dayOfMonth to it.amount
                }.groupBy { it.first }
                    .mapValues { entry ->
                        entry.value.sumOf { it.second.replace(",", ".").toDouble() }.toString()
                    }
                    .map { (dayOfMonth, totalAmount) ->
                        dayOfMonth to totalAmount
                    }

                setupChart()

                setupRecyclerView()
            }
        } else {
            incomeExpenseListModel.getIncomeExpenseListByYearAndIdCategoryModel(
                yearSearch.toString(),
                categoryId
            ).observe(this) { data ->
                categoryData = data.reversed().map { item ->
                    CategoryWithIncomeExpenseList(
                        incomeExpense = IncomeExpenseList(
                            id = item.incomeExpense.id,
                            note = item.incomeExpense.note,
                            amount = item.incomeExpense.amount,
                            date = item.incomeExpense.date,
                            categoryId = item.incomeExpense.categoryId,
                            type = item.incomeExpense.type,
                            image = item.incomeExpense.image,
                            categoryName = item.incomeExpense.categoryName,
                            iconResource = item.incomeExpense.iconResource,
                            accountId = item.incomeExpense.accountId
                        ),
                        category = item.category
                    )
                }

                val dataSetup = data.map { convertToIncomeExpenseListData(it) }

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                dateAmountList = dataSetup.map {
                    val date = LocalDate.parse(it.date, formatter)
                    val dayOfMonth = date.dayOfMonth
                    dayOfMonth to it.amount
                }.groupBy { it.first }
                    .mapValues { entry ->
                        entry.value.sumOf { it.second.replace(",", ".").toDouble() }.toString()
                    }
                    .map { (dayOfMonth, totalAmount) ->
                        dayOfMonth to totalAmount
                    }

                setupChart()

                setupRecyclerView()
            }
        }
    }

    private fun setupRecyclerView() {
        if (categoryData?.isEmpty() == true) {
            binding.listDataChart.visibility = View.GONE
            binding.emptyDataView.visibility = View.VISIBLE
            binding.chartCategory.visibility = View.GONE
        } else {
            binding.listDataChart.visibility = View.VISIBLE
            binding.emptyDataView.visibility = View.GONE
            binding.chartCategory.visibility = View.VISIBLE

            var totalAmount = 0f
            categoryData?.forEach { item ->
                val amountString = item.incomeExpense.amount.replace(",", ".")
                val amount = amountString.toFloatOrNull() ?: 0f
                totalAmount += amount
            }
            categoryData?.let { nonNullCategoryData ->
                val chartAdapter =
                    DataListChartAdapter(nonNullCategoryData, totalAmount, this, "detailedFragment")
                binding.recyclerViewChart.layoutManager = LinearLayoutManager(this)
                binding.recyclerViewChart.adapter = chartAdapter
            }
        }
    }

    private fun setupChart() {
        val entries = mutableListOf<Entry>()

        val dateAmountMap =
            dateAmountList?.associate { it.first to it.second.toFloat() } ?: emptyMap()

        for (i in 0 until 30) {
            val value = dateAmountMap[i + 1] ?: 0f
            val entry = Entry(i.toFloat(), value)
            entries.add(entry)
        }

        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.color = android.graphics.Color.GRAY
        lineDataSet.valueTextColor = if (checkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        lineDataSet.setDrawCircles(true)
        lineDataSet.setCircleColor(android.graphics.Color.YELLOW)
        lineDataSet.circleRadius = 4f

        lineDataSet.valueFormatter = MyValueFormatter()

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        lineChart.description.isEnabled = false

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = if (checkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        xAxis.setDrawGridLines(true)
        xAxis.gridColor = android.graphics.Color.DKGRAY
        xAxis.axisMinimum = 0f

        val axisLeft = lineChart.axisLeft
        axisLeft.textColor = if (checkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        axisLeft.setDrawGridLines(false)
        axisLeft.axisMinimum = 0f

        val axisRight = lineChart.axisRight
        axisRight.isEnabled = false

        lineChart.legend.isEnabled = false

        lineChart.invalidate()
    }


    class MyValueFormatter : ValueFormatter() {
        private val decimalFormat = DecimalFormat("#,###.##")

        override fun getFormattedValue(value: Float): String {
            return if (value == 0f) {
                ""
            } else {
                decimalFormat.format(value)
            }
        }
    }

    override fun onItemClick(data: CategoryWithIncomeExpenseList) {
        val dataConverter = IncomeExpenseListData(
            id = data.incomeExpense.id,
            note = data.incomeExpense.note,
            amount = data.incomeExpense.amount,
            date = data.incomeExpense.date,
            categoryId = data.incomeExpense.categoryId,
            type = data.incomeExpense.type,
            image = data.incomeExpense.image,
            categoryName = data.incomeExpense.categoryName,
            iconResource = data.incomeExpense.iconResource,
            idIcon = data.category.icon,
            accountId = data.incomeExpense.accountId
        )
        val gson = Gson()
        val json = gson.toJson(dataConverter)
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("itemDetail", json)
        startActivity(intent)
    }
}
