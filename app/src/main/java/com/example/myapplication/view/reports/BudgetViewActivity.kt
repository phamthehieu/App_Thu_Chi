package com.example.myapplication.view.reports

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.ListDetailBudgetAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.data.CombinedCategoryReport
import com.example.myapplication.databinding.ActivityBudgetViewBinding
import com.example.myapplication.entity.IncomeExpenseList
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
                        val totalAmount = items.sumOf { it.incomeExpense.amount.replace(",", ".").toDouble() }
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

}