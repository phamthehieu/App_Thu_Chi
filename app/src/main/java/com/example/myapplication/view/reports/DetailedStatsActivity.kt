package com.example.myapplication.view.reports

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.MonthlyReportAdapter
import com.example.myapplication.data.MonthlyReport
import com.example.myapplication.databinding.ActivityDetailedStatsBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class DetailedStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailedStatsBinding

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

        incomeExpenseListModel.allIncomeExpense.observe(this) { list ->
            val totalIncome = list.filter { it.type == "Income" }
                .sumOf { it.amount.replace(",", ".").toDouble() }
            val totalExpense = list.filter { it.type == "Expense" }
                .sumOf { it.amount.replace(",", ".").toDouble() }
            val totalBalance = totalIncome - totalExpense

            val decimalFormat = DecimalFormat("#,###.##")
            val groupedIncomeExpenseList = groupAndSumByYearMonthAndType(list)
            val monthlyReports = transformData(groupedIncomeExpenseList)
            val adapter = MonthlyReportAdapter(monthlyReports)

            binding.totalBalanceTv.text = decimalFormat.format(totalBalance)
            binding.allExpenseTv.text = decimalFormat.format(totalExpense)
            binding.allIncomeTv.text = decimalFormat.format(totalIncome)

            binding.recyclerViewDetailedStats.layoutManager = LinearLayoutManager(this)
            binding.recyclerViewDetailedStats.adapter = adapter
        }
    }

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

}