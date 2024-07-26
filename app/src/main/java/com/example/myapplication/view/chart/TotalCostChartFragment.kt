package com.example.myapplication.view.chart

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.LegendAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.LegendItem
import com.example.myapplication.databinding.FragmentTotalChartBinding
import com.example.myapplication.databinding.FragmentTotalCostChartBinding
import com.example.myapplication.utilities.ColorUtils
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.DecimalFormat

class TotalCostChartFragment : Fragment() {

    private lateinit var binding: FragmentTotalCostChartBinding
    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView

    private var groupedData: List<CategoryWithIncomeExpenseList>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupedData = it.getParcelableArrayList("groupedData")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTotalCostChartBinding.inflate(inflater, container, false)

        pieChart = binding.pieChart
        recyclerView = binding.recyclerViewDataLegend

        if (groupedData.isNullOrEmpty()) {
            binding.chartCl.visibility = View.GONE
            binding.emptyDataView.visibility = View.VISIBLE
        } else {
            setupPieChart()
            binding.chartCl.visibility = View.VISIBLE
            binding.emptyDataView.visibility = View.GONE
        }

        return binding.root
    }

    private fun setupPieChart() {
        val list: ArrayList<PieEntry> = ArrayList()
        val colors = ColorUtils.generateRandomLightColors(groupedData?.size ?: 0)

        var totalAmount = 0f
        groupedData?.forEach { item ->
            val amountString = item.incomeExpense.amount.replace(",", ".")
            val amount = amountString.toFloatOrNull() ?: 0f
            list.add(PieEntry(amount, item.category.name ?: ""))
            totalAmount += amount
        }

        val expenseFormatter = DecimalFormat("#,###.##")
        val formattedTotalBalance = expenseFormatter.format(totalAmount)
        val pieDataSet = PieDataSet(list, "")
        pieDataSet.colors = colors
        pieDataSet.setDrawValues(false)

        val pieData = PieData(pieDataSet)

        pieChart.data = pieData

        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.centerText = formattedTotalBalance
        pieChart.animateY(2000)
        pieChart.legend.isEnabled = false

        recyclerView.layoutManager = LinearLayoutManager(context)
        val items = list.mapIndexed { index, pieEntry ->
            LegendItem(pieEntry.label, colors[index], pieEntry.value.toString())
        }
        val adapter = LegendAdapter(items, totalAmount)
        recyclerView.adapter = adapter

        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val pieEntry = e as PieEntry
                val selectedCategory = pieEntry.label

                items.forEach { it.isSelected = it.category == selectedCategory }
                adapter.notifyDataSetChanged()

                val selectedIndex = items.indexOfFirst { it.category == selectedCategory }
                if (selectedIndex != -1) {
                    recyclerView.scrollToPosition(selectedIndex)
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onNothingSelected() {
                items.forEach { it.isSelected = false }
                adapter.notifyDataSetChanged()
            }
        })
    }

}