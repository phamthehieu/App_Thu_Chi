package com.example.myapplication.view.chart

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.DataListChartAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityDetailedChartOfCategoryBinding
import com.example.myapplication.entity.IncomeExpenseList
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

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedChartOfCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lineChart = binding.lineChart

        val categoryId = intent.getIntExtra("categoryId", 0)
        val categoryName = intent.getStringExtra("categoryName")

        binding.titleText.text = categoryName

        getDataSource(categoryId)

    }

    @SuppressLint("DefaultLocale")
    private fun getDataSource(categoryId: Int) {
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
                        iconResource = item.incomeExpense.iconResource
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

    private fun setupRecyclerView() {
        var totalAmount = 0f
        categoryData?.forEach { item ->
            val amountString = item.incomeExpense.amount.replace(",", ".")
            val amount = amountString.toFloatOrNull() ?: 0f
            totalAmount += amount
        }
        categoryData?.let { nonNullCategoryData ->
            val chartAdapter = DataListChartAdapter(nonNullCategoryData, totalAmount, this, "detailedFragment")
            binding.recyclerViewChart.layoutManager = LinearLayoutManager(this)
            binding.recyclerViewChart.adapter = chartAdapter
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
        lineDataSet.valueTextColor = android.graphics.Color.WHITE
        lineDataSet.setDrawCircles(true)
        lineDataSet.setCircleColor(android.graphics.Color.YELLOW)
        lineDataSet.circleRadius = 4f

        lineDataSet.valueFormatter = MyValueFormatter()

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        lineChart.description.isEnabled = false

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = android.graphics.Color.WHITE
        xAxis.setDrawGridLines(true)
        xAxis.gridColor = android.graphics.Color.DKGRAY

        val axisLeft = lineChart.axisLeft
        axisLeft.textColor = android.graphics.Color.WHITE
        axisLeft.setDrawGridLines(false)

        val axisRight = lineChart.axisRight
        axisRight.isEnabled = false

        lineChart.legend.isEnabled = false

        lineChart.invalidate()
    }

    class MyValueFormatter : ValueFormatter() {
        private val decimalFormat = DecimalFormat("#,###.##")

        override fun getFormattedValue(value: Float): String {
            return decimalFormat.format(value)
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
            idIcon = data.category.icon
        )
        val gson = Gson()
        val json = gson.toJson(dataConverter)
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("itemDetail", json)
        startActivity(intent)
    }
}
