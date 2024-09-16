package com.example.myapplication.view.reports

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerReportAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.CombinedCategoryReport
import com.example.myapplication.databinding.FragmentReportsBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import java.text.DecimalFormat
import java.util.Calendar

class ReportsFragment : Fragment() {

    private lateinit var binding: FragmentReportsBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    
    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportsBinding.inflate(inflater, container, false)

        viewPager = binding.viewPagerSetting
        adapter = ViewPagerReportAdapter(requireActivity())
        viewPager.adapter = adapter

        binding.tabExpenseChart.setOnClickListener {
            viewPager.currentItem = 0
        }

        binding.tabIncomeChart.setOnClickListener {
            viewPager.currentItem = 1
        }

        updateTabBackground(1)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabBackground(position + 1)
            }
        })

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    
    private fun updateTabBackground(selectedTabNumber: Int) {
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
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100_black)

                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow
                    )
                )
                val color = ContextCompat.getColor(requireContext(), R.color.yellow)
                requireActivity().window.statusBarColor = color
            }
        }
    }


}