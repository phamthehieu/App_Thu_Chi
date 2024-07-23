package com.example.myapplication.adapter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.view.chart.TotalChartFragment
import com.example.myapplication.view.chart.TotalCostChartFragment
import com.example.myapplication.view.chart.TotalIncomeChartFragment

class ChartPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val groupedData: List<CategoryWithIncomeExpenseList>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> TotalChartFragment()
            1 -> TotalCostChartFragment()
            2 -> TotalIncomeChartFragment()
            else -> TotalChartFragment()
        }

        val filteredData = when (position) {
            1 -> groupedData.filter { it.category.source == "Expense" }
            2 -> groupedData.filter { it.category.source == "Income" }
            else -> groupedData
        }

        fragment.arguments = Bundle().apply {
            putParcelableArrayList("groupedData", ArrayList(filteredData))
        }
        return fragment
    }
}
