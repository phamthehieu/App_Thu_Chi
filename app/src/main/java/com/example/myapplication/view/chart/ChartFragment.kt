package com.example.myapplication.view.chart

import android.annotation.SuppressLint
import android.content.res.Configuration
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

class ChartFragment : Fragment(), DateChartAdapter.OnDateClickListener {

    private lateinit var binding: FragmentChartBinding
    private val monthsList = mutableListOf<String>()
    private lateinit var adapter: DateChartAdapter
    private var currentMonthIndex = 0

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var incomeExpenseList = listOf<IncomeExpenseListData>()

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

        binding.tabExpenseChart.setOnClickListener {
            updateTabBackground(1)
        }

        binding.tabIncomeChart.setOnClickListener {
            updateTabBackground(2)
        }

        updateTabBackground(1)

        adapter = DateChartAdapter(monthsList, {
            loadMoreMonths()
        }, this)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
        binding.recyclerViewDateChart.layoutManager = layoutManager
        binding.recyclerViewDateChart.adapter = adapter

        loadMoreMonths()

        getDataSource()

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun getDataSource() {
        val formattedMonth = String.format("%02d", monthSearch)
        incomeExpenseListModel.getIncomeExpenseListByMonthYear(
            yearSearch.toString(),
            formattedMonth
        ).observe(viewLifecycleOwner) { data ->
            incomeExpenseList = data.map { convertToIncomeExpenseListData(it) }
            val groupedData = data.groupBy { it.category.id }
                .map { (categoryId, items) ->
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
                            iconResource = items.first().incomeExpense.iconResource
                        ),
                        category = items.first().category
                    )
                }
            chartViewPager(groupedData)
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

        if (filteredData.isEmpty()) {
            binding.chartRL.visibility = View.GONE
            binding.listDataChart.visibility = View.GONE
            binding.emptyDataView.visibility = View.VISIBLE
        } else {
            binding.chartRL.visibility = View.VISIBLE
            binding.listDataChart.visibility = View.VISIBLE
            binding.emptyDataView.visibility = View.GONE
        }
        Log.d("Hieu157", "$incomeExpenseList")
        var totalAmount = 0f
        filteredData.forEach { item ->
            val amountString = item.incomeExpense.amount.replace(",", ".")
            val amount = amountString.toFloatOrNull() ?: 0f
            totalAmount += amount
        }

        val chartAdapter = DataListChartAdapter(filteredData, totalAmount)
        binding.recyclerViewChart.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewChart.adapter = chartAdapter
    }


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadMoreMonths() {
        val newMonths = getPreviousMonths(currentMonthIndex, 10)
        val oldSize = monthsList.size
        monthsList.addAll(newMonths)
        currentMonthIndex += newMonths.size
        adapter.notifyItemRangeInserted(oldSize, newMonths.size)

        if (monthsList.isNotEmpty()) {
            val position = monthsList.size - newMonths.size
            (binding.recyclerViewDateChart.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(position, 0)
        }
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
        getDataSource()
    }

    private fun updateTabBackground(selectedTabNumber: Int) {
        val currentNightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
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
                binding.imageEmptyData.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray1
                    )
                )
                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grayHeader
                    )
                )
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
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100_black)
                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray1
                    )
                )
                binding.imageEmptyData.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray1
                    )
                )
            }
        }
    }
}