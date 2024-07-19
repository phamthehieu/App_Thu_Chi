package com.example.myapplication.view.chart

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.DateChartAdapter
import com.example.myapplication.databinding.FragmentChartBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ChartFragment : Fragment(), DateChartAdapter.OnDateClickListener {

    private lateinit var binding: FragmentChartBinding
    private val monthsList = mutableListOf<String>()
    private lateinit var adapter: DateChartAdapter
    private var currentMonthIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

        return binding.root
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
                        R.color.yellow
                    )
                )
            }
        }
    }

    override fun onDateClick(date: String) {
        Log.d("Hieu60", "Selected date: $date")
    }
}