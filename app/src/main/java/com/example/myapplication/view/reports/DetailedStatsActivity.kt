package com.example.myapplication.view.reports

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.MonthPagerAdapter
import com.example.myapplication.adapter.MonthlyReportAdapter
import com.example.myapplication.data.MonthlyReport
import com.example.myapplication.databinding.ActivityDetailedStatsBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class DetailedStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailedStatsBinding

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var checkByYear = false

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.popupCalenderBtn.setOnClickListener {
            showCustomDialogYear()
        }

        getData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getData() {
        if (checkByYear) {
            incomeExpenseListModel.getFilteredIncomeExpenseListYear(
                yearSearch.toString()
            ).observe(this) { data ->
                val groupedData = data.map {
                    IncomeExpenseList(
                        id = it.incomeExpense.id,
                        note = it.incomeExpense.note,
                        amount = it.incomeExpense.amount,
                        date = it.incomeExpense.date,
                        categoryId = it.category.id,
                        type = it.incomeExpense.type,
                        image = it.incomeExpense.image,
                        categoryName = it.category.name,
                        iconResource = it.incomeExpense.iconResource,
                        accountId = it.incomeExpense.accountId
                    )
                }
                val totalIncome = groupedData.filter { it.type == "Income" }
                    .sumOf { it.amount.replace(",", ".").toDouble() }
                val totalExpense = groupedData.filter { it.type == "Expense" }
                    .sumOf { it.amount.replace(",", ".").toDouble() }
                val totalBalance = totalIncome - totalExpense

                val decimalFormat = DecimalFormat("#,###.##")

                val groupedIncomeExpenseList = groupAndSumByYearMonthAndType(groupedData)

                val monthlyReports = transformDataWithoutHeaders(groupedIncomeExpenseList)
                val adapter = MonthlyReportAdapter(monthlyReports, showHeaders = false)

                binding.totalBalanceTv.text = decimalFormat.format(totalBalance)
                binding.allExpenseTv.text = decimalFormat.format(totalExpense)
                binding.allIncomeTv.text = decimalFormat.format(totalIncome)

                binding.recyclerViewDetailedStats.layoutManager = LinearLayoutManager(this)
                binding.recyclerViewDetailedStats.adapter = adapter
            }
        } else {
            incomeExpenseListModel.allIncomeExpense.observe(this) { list ->
                val totalIncome = list.filter { it.type == "Income" }
                    .sumOf { it.amount.replace(",", ".").toDouble() }
                val totalExpense = list.filter { it.type == "Expense" }
                    .sumOf { it.amount.replace(",", ".").toDouble() }
                val totalBalance = totalIncome - totalExpense

                val decimalFormat = DecimalFormat("#,###.##")
                val groupedIncomeExpenseList = groupAndSumByYearMonthAndType(list)
                val monthlyReports = transformData(groupedIncomeExpenseList)
                val adapter = MonthlyReportAdapter(monthlyReports, showHeaders = true)

                binding.totalBalanceTv.text = decimalFormat.format(totalBalance)
                binding.allExpenseTv.text = decimalFormat.format(totalExpense)
                binding.allIncomeTv.text = decimalFormat.format(totalIncome)

                binding.recyclerViewDetailedStats.layoutManager = LinearLayoutManager(this)
                binding.recyclerViewDetailedStats.adapter = adapter
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun transformDataWithoutHeaders(groupedData: Map<String, List<Map<String, Any>>>): List<MonthlyReport> {
        val monthlyReports = mutableListOf<MonthlyReport>()

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val outputFormatter = DateTimeFormatter.ofPattern("'Thg' M", Locale.getDefault())

        groupedData.forEach { (_, records) ->
            val months = records.groupBy { it["date"] as String }
            months.forEach { (month, values) ->
                val expense =
                    values.filter { it["type"] == "Expense" }.sumOf { it["totalAmount"] as Double }
                val income =
                    values.filter { it["type"] == "Income" }.sumOf { it["totalAmount"] as Double }
                val balance = income - expense

                val yearMonth = YearMonth.parse(month, inputFormatter)
                val formattedMonth = yearMonth.format(outputFormatter)

                monthlyReports.add(MonthlyReport(formattedMonth, expense, income, balance))
            }
        }

        return monthlyReports
    }


    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun groupAndSumByYearMonthAndType(data: List<IncomeExpenseList>): Map<String, List<Map<String, Any>>> {
        return data.groupBy {
            val date = LocalDate.parse(it.date)
            "${date.year}-${String.format("%02d", date.monthValue)}-${it.type}"
        }.map { (key, value) ->
            val parts = key.split("-")
            val year = parts[0]
            val date = "${parts[0]}-${parts[1]}"
            val type = parts[2]

            val totalAmount = value.sumOf { it.amount.replace(',', '.').toDouble() }

            year to mapOf(
                "date" to date,
                "totalAmount" to totalAmount,
                "type" to type
            )
        }.groupBy({ it.first }, { it.second })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun transformData(groupedData: Map<String, List<Map<String, Any>>>): List<Any> {
        val monthlyReports = mutableListOf<Any>()

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

        val outputFormatter = DateTimeFormatter.ofPattern("'Thg' M", Locale.getDefault())

        groupedData.forEach { (year, records) ->
            monthlyReports.add(year)
            val months = records.groupBy { it["date"] as String }
            months.forEach { (month, values) ->
                val expense =
                    values.filter { it["type"] == "Expense" }.sumOf { it["totalAmount"] as Double }
                val income =
                    values.filter { it["type"] == "Income" }.sumOf { it["totalAmount"] as Double }
                val balance = income - expense

                val yearMonth = YearMonth.parse(month, inputFormatter)
                val formattedMonth = yearMonth.format(outputFormatter)

                monthlyReports.add(MonthlyReport(formattedMonth, expense, income, balance))
            }
        }

        return monthlyReports
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            checkByYear = false
            binding.monthTv.text = "Tất cả"
            getData()
            dialog.dismiss()
        }
        successBtn.setOnClickListener {
            checkByYear = true
            binding.monthTv.text = "Năm $yearSearch"
            getData()
            dialog.dismiss()
        }
        dialog.show()
    }

}