package com.example.myapplication.view.reports

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.ListDetailBudgetAdapter
import com.example.myapplication.adapter.MonthPagerAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.data.CombinedCategoryReport
import com.example.myapplication.databinding.ActivityBudgetViewBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import java.text.DecimalFormat
import java.util.Calendar

class BudgetViewActivity : AppCompatActivity(), ListDetailBudgetAdapter.OnItemClickListenerDetail {

    private lateinit var binding: ActivityBudgetViewBinding

    private lateinit var listDetailBudgetAdapter: ListDetailBudgetAdapter

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(this.application)
    }

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var check = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addNewBudget.setOnClickListener {
            val settingBudgetIntent = Intent(this, SettingBudgetActivity::class.java)
            startActivity(settingBudgetIntent)
        }

        binding.popupCalenderBtn.setOnClickListener {
            showCustomDialogBox()
        }

        binding.monthTv.text = "Thg $monthSearch $yearSearch"

        setupData()

    }

    private fun setupData() {
        categoryViewModel.allCategory.observe(this, Observer { categoriesWithIcons ->
            categoriesWithIcons?.let { categories ->
                val filteredCategories = categories.filter {
                    it.category.source == "Expense" && it.category.budget > 0.toString()
                }
                var combinedList = filteredCategories.map { categoryWithIcon ->
                    CombinedCategoryReport(
                        categoryName = categoryWithIcon.category.name,
                        categoryType = categoryWithIcon.category.type,
                        iconResource = categoryWithIcon.icon.iconResource,
                        iconType = categoryWithIcon.icon.type,
                        source = categoryWithIcon.category.source,
                        idCategory = categoryWithIcon.category.id,
                        icon = categoryWithIcon.category.icon,
                        budget = categoryWithIcon.category.budget,
                        totalAmount = "0"
                    )
                }

                val formattedMonth = String.format("%02d", monthSearch)
                incomeExpenseListModel.getIncomeExpenseListByMonthYear(
                    yearSearch.toString(),
                    formattedMonth
                ).observe(this) { data ->
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
                                iconResource = items.first().incomeExpense.iconResource
                            ),
                            category = items.first().category
                        )
                    }

                    combinedList = combinedList.map { combinedCategoryIcon ->
                        val matchingCategoryWithIncomeExpense =
                            groupedData.find { it.category.id == combinedCategoryIcon.idCategory }
                        combinedCategoryIcon.copy(
                            totalAmount = matchingCategoryWithIncomeExpense?.incomeExpense?.amount
                                ?: "0"
                        )
                    }

                    renderDataRecyclerView(combinedList)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun renderDataRecyclerView(combinedList: List<CombinedCategoryReport>) {
        val totalAmountSum =
            combinedList.sumOf { it.totalAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 }
        val budgetSum = combinedList.sumOf { it.budget.replace(',', '.').toDoubleOrNull() ?: 0.0 }
        val decimalFormat = DecimalFormat("#,###.##")

        binding.totalCategoryDetaiTV.text = decimalFormat.format(totalAmountSum)
        binding.budgetTotalDetai.text = decimalFormat.format(budgetSum)

        val remaining = budgetSum - totalAmountSum

        val remainingPercentage = if (budgetSum != 0.0) {
            (remaining / budgetSum) * 100
        } else {
            0.0
        }

        if (remainingPercentage > 0) {
            binding.progressBarCircularDetai.progress = remainingPercentage.toInt()
            binding.progressTextDetai.text = "${decimalFormat.format(remainingPercentage)}%"
        } else {
            binding.progressBarCircularDetai.progress = 0
            binding.progressTextDetai.text = "__"
        }

        binding.remainingTotalDetailTv.text = decimalFormat.format(remaining)
        binding.recyclerViewBudget.layoutManager = LinearLayoutManager(this)
        listDetailBudgetAdapter = ListDetailBudgetAdapter(combinedList, this)
        binding.recyclerViewBudget.adapter = listDetailBudgetAdapter
    }

    override fun editBudget(category: CombinedCategoryReport) {
        val data = CombinedCategoryIcon(
            categoryName = category.categoryName,
            categoryType = category.categoryType,
            iconResource = category.iconResource,
            iconType = category.iconType,
            source = category.source,
            idCategory = category.idCategory,
            icon = category.icon,
            budget = category.budget
        )
        val keyboard = KeyBroadBottomReportsFragment()
        keyboard.setupDataCategory(data)
        keyboard.show(supportFragmentManager, "keyboard")
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialogBox() {
        val months = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val years = (currentYear - 50..currentYear + 50).toList()
        val dialog = Dialog(this)
        var yearData = Calendar.getInstance().get(Calendar.YEAR)

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
            this,
            months,
            false,
            yearSearch,
            currentMonth,
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
                    this,
                    years,
                    true,
                    yearSearch,
                    currentMonth,
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
                    this,
                    months,
                    false,
                    yearSearch,
                    currentMonth,
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
            binding.monthTv.text = "Thg $monthSearch $yearSearch"
            setupData()
            dialog.dismiss()
        }
        dialog.show()
    }

}