package com.example.myapplication.view.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.IncomeExpenseListAdapter
import com.example.myapplication.adapter.MonthPagerAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import java.text.DecimalFormat
import java.util.Calendar

class HomeFragment : Fragment(), OnMonthSelectedListener {

    private lateinit var binding: FragmentHomeBinding

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var check = true
    private lateinit var adapter: IncomeExpenseListAdapter

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onMonthSelected(month: Int) {
        if (!check) {
            yearSearch = month
        } else {
            monthSearch = month + 1
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val currentNightMode = requireActivity().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.searchBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black1
                    )
                )

                binding.calendarBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black1
                    )
                )
                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow
                    )
                )
                val color = ContextCompat.getColor(requireContext(), R.color.yellow)
                requireActivity().window.statusBarColor = color
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                binding.searchBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.calendarBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

            }
        }

        binding.popupCalenderBtn.setOnClickListener {
            showCustomDialogBox()
        }

        binding.monthTv.text = "Thg $monthSearch"

        binding.recyclerViewHome.layoutManager = LinearLayoutManager(requireContext())

        setupBackground()

        return binding.root;
    }

    @SuppressLint("DefaultLocale")
    private fun setupBackground() {
        val formattedMonth = String.format("%02d", monthSearch)
        incomeExpenseListModel.getIncomeExpenseListByMonthYear(yearSearch.toString(), formattedMonth).observe(viewLifecycleOwner) { incomeExpenseList ->

            val totalIncome = incomeExpenseList.filter { it.type == "Income" }
                .sumOf { it.amount.replace(",", ".").toDouble() }
            val totalExpense = incomeExpenseList.filter { it.type == "Expense" }
                .sumOf { it.amount.replace(",", ".").toDouble() }
            val expenseFormatter = DecimalFormat("#,###.##")
            val formattedExpense = expenseFormatter.format(totalIncome)
            val formattedIncome = expenseFormatter.format(totalExpense)
            binding.costTv.text = formattedIncome
            binding.IncomeTv.text =  formattedExpense
            val totalBalance = totalIncome - totalExpense
            val formattedTotalBalance = expenseFormatter.format(totalBalance)
            binding.totalBalanceTv.text = formattedTotalBalance

            val groupedIncomeExpenseList = groupIconsByType(incomeExpenseList)
            adapter = IncomeExpenseListAdapter(groupedIncomeExpenseList)
            binding.recyclerViewHome.adapter = adapter
        }
    }

    private fun groupIconsByType(data: List<IncomeExpenseList>): Map<String, List<IncomeExpenseList>> {
        return data.groupBy { it.date }
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialogBox() {
        context?.let { ctx ->
            val months = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val years = (currentYear - 50..currentYear + 50).toList()
            val dialog = Dialog(ctx)

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

            fun updateTitleRecord(month: Int, year: Int) {
                titleRecord.text = "tháng $month năm $year"
                textYearTitle.text = year.toString()
            }
            updateTitleRecord(monthSearch, yearSearch)

            val viewPager: ViewPager2 = dialog.findViewById(R.id.viewPager)
            var monthPagerAdapter = MonthPagerAdapter(
                requireContext(),
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
                        requireContext(),
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
                    viewPager.adapter = monthPagerAdapter
                    viewPager.offscreenPageLimit = 1
                    viewPager.isUserInputEnabled = false
                    yearIcon.setBackgroundResource(R.drawable.ic_sort_up_24)
                    check = false
                } else {
                    monthPagerAdapter = MonthPagerAdapter(
                        requireContext(),
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
                binding.yearTv.text = "năm $yearSearch"
                binding.monthTv.text = "Thg $monthSearch"
                setupBackground()
                dialog.dismiss()
            }
            dialog.show()
        }
    }
}