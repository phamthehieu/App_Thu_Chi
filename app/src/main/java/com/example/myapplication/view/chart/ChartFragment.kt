package com.example.myapplication.view.chart

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.adapter.ChartPagerAdapter
import com.example.myapplication.adapter.DataListChartAdapter
import com.example.myapplication.adapter.DateChartAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.FragmentChartBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.utilities.convertToIncomeExpenseListData
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.github.mikephil.charting.data.PieEntry
import java.time.LocalDate
import java.util.Calendar

class ChartFragment : Fragment(), DateChartAdapter.OnDateClickListener,
    DataListChartAdapter.OnCategoryClickListener {

    private lateinit var binding: FragmentChartBinding
    private val monthsList = mutableListOf<String>()
    private lateinit var adapter: DateChartAdapter
    private var currentMonthIndex = 0

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var checkSearch: Int = 1

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChartBinding.inflate(inflater, container, false)

        adapter = DateChartAdapter(monthsList, {
            loadMoreDate()
        }, this)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
        binding.recyclerViewDateChart.layoutManager = layoutManager
        binding.recyclerViewDateChart.adapter = adapter

        updateTabBackground(1)

        getDataSource()

        loadMoreDate()

        binding.tabExpenseChart.setOnClickListener {
            updateTabBackground(1)
            getDataSource()
        }

        binding.tabIncomeChart.setOnClickListener {
            updateTabBackground(2)
            getDataSource()
        }

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun getDataSource() {
        if (checkSearch == 1) {
            val formattedMonth = String.format("%02d", monthSearch)
            incomeExpenseListModel.getIncomeExpenseListByMonthYear(
                yearSearch.toString(),
                formattedMonth
            ).observe(viewLifecycleOwner) { data ->
                val groupedData = data.groupBy { it.category.id }.map { (categoryId, items) ->
                    val totalAmount =
                        items.sumOf { it.incomeExpense.amount.replace(",", ".").toDouble() }
                    CategoryWithIncomeExpenseList(
                        incomeExpense = IncomeExpenseList(
                            id = items.first().incomeExpense.id,
                            note = items.first().incomeExpense.note,
                            amount = totalAmount.toString(),
                            date = items.first().incomeExpense.date,
                            categoryId = items.first().incomeExpense.categoryId,
                            type = items.first().incomeExpense.type,
                            image = items.first().incomeExpense.image,
                            categoryName = items.first().incomeExpense.categoryName,
                            iconResource = items.first().incomeExpense.iconResource,
                            accountId = items.first().incomeExpense.accountId
                        ),
                        category = items.first().category
                    )
                }
                chartViewPager(groupedData)
            }
        } else {
            incomeExpenseListModel.getFilteredIncomeExpenseListYear(
                yearSearch.toString()
            ).observe(viewLifecycleOwner) { data ->
                val groupedData = data.groupBy { it.category.id }.map { (categoryId, items) ->
                    val totalAmount =
                        items.sumOf { it.incomeExpense.amount.replace(",", ".").toDouble() }
                    CategoryWithIncomeExpenseList(
                        incomeExpense = IncomeExpenseList(
                            id = items.first().incomeExpense.id,
                            note = items.first().incomeExpense.note,
                            amount = totalAmount.toString(),
                            date = items.first().incomeExpense.date,
                            categoryId = items.first().incomeExpense.categoryId,
                            type = items.first().incomeExpense.type,
                            image = items.first().incomeExpense.image,
                            categoryName = items.first().incomeExpense.categoryName,
                            iconResource = items.first().incomeExpense.iconResource,
                            accountId = items.first().incomeExpense.accountId
                        ),
                        category = items.first().category
                    )
                }
                chartViewPager(groupedData)
            }
        }
    }

    private fun chartViewPager(groupedData: List<CategoryWithIncomeExpenseList>) {
        val viewPager2 = binding.viewPager2Chart
        val indicator = binding.indicator

        val fragmentActivity = requireActivity()

        val adapterChartPage = ChartPagerAdapter(fragmentActivity, groupedData)
        viewPager2.adapter = adapterChartPage

        indicator.setViewPager(viewPager2)

        updateChartAndList(groupedData, 0)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateChartAndList(groupedData, position)
            }
        })
    }

    private fun updateChartAndList(
        groupedData: List<CategoryWithIncomeExpenseList>,
        position: Int
    ) {
        val filteredData = when (position) {
            1 -> groupedData.filter { it.category.source == "Expense" }
            2 -> groupedData.filter { it.category.source == "Income" }
            else -> groupedData
        }

        var totalAmount = 0f
        filteredData.forEach { item ->
            val amountString = item.incomeExpense.amount.replace(",", ".")
            val amount = amountString.toFloatOrNull() ?: 0f
            totalAmount += amount
        }

        val chartAdapter = DataListChartAdapter(filteredData, totalAmount, this, "chartFragment")
        binding.recyclerViewChart.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewChart.adapter = chartAdapter
    }


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadMoreDate() {
        monthsList.clear()
        currentMonthIndex = 0

        if (checkSearch == 1) {
            val newMonths = getPreviousMonths(currentMonthIndex, 10)
            monthsList.addAll(newMonths)
            currentMonthIndex += newMonths.size
        } else {
            val newYears = getPreviousYears(currentMonthIndex, 10)
            monthsList.addAll(newYears)
            currentMonthIndex += newYears.size
        }

        adapter.notifyDataSetChanged()

        if (monthsList.isNotEmpty()) {
            (binding.recyclerViewDateChart.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(0, 0)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPreviousYears(startIndex: Int, count: Int): List<String> {
        val currentYear = LocalDate.now().year
        val yearsList = mutableListOf<String>()

        for (i in startIndex until startIndex + count) {
            val year = currentYear - i
            yearsList.add(year.toString())
        }

        return yearsList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPreviousMonths(startIndex: Int, count: Int): List<String> {
        val monthNames = listOf(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        )

        val currentDate = LocalDate.now()
        val monthsList = mutableListOf<String>()

        for (i in startIndex until startIndex + count) {
            val previousMonth = currentDate.minusMonths(i.toLong())
            val monthIndex = previousMonth.monthValue - 1
            val formattedMonth = monthNames[monthIndex] + " " + previousMonth.year

            val displayMonth = when {
                previousMonth.year == currentDate.year && previousMonth.month == currentDate.month -> "Tháng này"
                previousMonth.year == currentDate.year && previousMonth.month == currentDate.month.minus(
                    1
                ) -> "Tháng trước"

                else -> formattedMonth
            }

            monthsList.add(displayMonth)
        }

        return monthsList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateClick(date: String) {
        val currentDate = LocalDate.now()
        if (checkSearch == 1) {
            when (date) {
                "Tháng này" -> {
                    monthSearch = currentDate.monthValue
                    yearSearch = currentDate.year
                }

                "Tháng trước" -> {
                    val previousMonthDate = currentDate.minusMonths(1)
                    monthSearch = previousMonthDate.monthValue
                    yearSearch = previousMonthDate.year
                }

                else -> {
                    val parts = date.split(" ")
                    if (parts.size == 3) {
                        monthSearch = parts[1].toInt()
                        yearSearch = parts[2].toInt()
                    }
                }
            }
        } else {
            yearSearch = date.toInt()
        }
        getDataSource()
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTabBackground(selectedTabNumber: Int) {
        checkSearch = selectedTabNumber
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val selectedColor = resources.getColor(R.color.black, requireContext().theme)
                val unselectedColor = resources.getColor(R.color.white, requireContext().theme)

                binding.tabExpenseChart.apply {
                    setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_white_left else 0)
                    setTextColor(if (selectedTabNumber == 1) selectedColor else unselectedColor)
                }
                binding.tabIncomeChart.apply {
                    setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_right else 0)
                    setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
                }
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100)

                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grayHeader
                    )
                )
                val color = ContextCompat.getColor(requireContext(), R.color.grayHeader)
                requireActivity().window.statusBarColor = color
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                val selectedColor = resources.getColor(R.color.white, requireContext().theme)
                val unselectedColor = resources.getColor(R.color.black, requireContext().theme)

                binding.tabExpenseChart.apply {
                    setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_white_left_black else 0)
                    setTextColor(if (selectedTabNumber == 1) selectedColor else unselectedColor)
                }
                binding.tabIncomeChart.apply {
                    setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_right_black else 0)
                    setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
                }
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_black10_100)
                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray1
                    )
                )
                binding.titleBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow))
                val color = ContextCompat.getColor(requireContext(), R.color.yellow)
                requireActivity().window.statusBarColor = color
            }
        }
        yearSearch = Calendar.getInstance().get(Calendar.YEAR)
        monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
        adapter.selectedPosition = 0
        adapter.notifyDataSetChanged()
        loadMoreDate()
    }

    override fun onItemClick(data: CategoryWithIncomeExpenseList) {
        val intent = Intent(requireContext(), DetailedChartOfCategoryActivity::class.java)
        intent.putExtra("categoryId", data.incomeExpense.categoryId)
        intent.putExtra("categoryName", data.incomeExpense.categoryName)
        intent.putExtra("yearSearch", yearSearch)
        intent.putExtra("monthSearch", monthSearch)
        intent.putExtra("checkSearchData", checkSearch)
        startActivity(intent)
    }
}