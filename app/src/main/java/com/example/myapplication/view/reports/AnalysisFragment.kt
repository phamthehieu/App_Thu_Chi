package com.example.myapplication.view.reports

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.myapplication.R
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.CombinedCategoryReport
import com.example.myapplication.databinding.FragmentAnalysisBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import java.text.DecimalFormat
import java.util.Calendar

class AnalysisFragment : Fragment() {

    private lateinit var binding: FragmentAnalysisBinding

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(requireActivity().application)
    }

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(requireActivity().application)
    }

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false)

        binding.budgetTotal.setOnClickListener {
            val budgetViewIntent = Intent(requireContext(), BudgetViewActivity::class.java)
            startActivity(budgetViewIntent)
        }

        binding.viewMonthlyStatistics.setOnClickListener {
            val detailedStatsIntent = Intent(requireContext(), DetailedStatsActivity::class.java)
            startActivity(detailedStatsIntent)
        }

        setupData()

        setupNightMode()

        return binding.root
    }

    private fun setupNightMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.loadMoreButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                binding.loadMoreAccount.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
            }

            Configuration.UI_MODE_NIGHT_NO -> {
               binding.loadMoreButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
                binding.loadMoreAccount.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun setupData() {
        categoryViewModel.allCategory.observe(requireActivity(), Observer { categoriesWithIcons ->
            categoriesWithIcons?.let { categories ->

//                val filteredCategories = categories.filter { it.category.source == "Expense" && it.category.budget > 0.toString() }
                val combinedList = categories.map { categoryWithIcon ->
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
                ).observe(requireActivity()) { data ->
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

                    val totalIncome = groupedData.filter { it.category.source == "Income" }
                        .sumOf { it.incomeExpense.amount.replace(",", ".").toDouble() }
                    val totalExpense = groupedData.filter { it.category.source == "Expense" }
                        .sumOf { it.incomeExpense.amount.replace(",", ".").toDouble() }
                    val budgetAll =
                        groupedData.sumOf { it.category.budget.replace(",", ".").toDouble() }

                    val decimalFormat = DecimalFormat("#,###.##")

                    val surplus = totalIncome - totalExpense

                    binding.monthAllTv.text = "Thg $monthSearch"
                    binding.expenseAllTv.text = decimalFormat.format(totalExpense)
                    binding.incomeAllTv.text = decimalFormat.format(totalIncome)
                    binding.surplusAllTv.text = decimalFormat.format(surplus)

                    val dataFormat = combinedList.map { combinedCategoryIcon ->
                        val matchingCategoryWithIncomeExpense =
                            groupedData.find { it.category.id == combinedCategoryIcon.idCategory }
                        combinedCategoryIcon.copy(
                            totalAmount = matchingCategoryWithIncomeExpense?.incomeExpense?.amount
                                ?: "0"
                        )
                    }

                    renderDataView(dataFormat)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun renderDataView(combinedList: List<CombinedCategoryReport>) {
        val totalAmountSum =
            combinedList.sumOf { it.totalAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 }
        val budgetSum = combinedList.sumOf { it.budget.replace(',', '.').toDoubleOrNull() ?: 0.0 }
        val decimalFormat = DecimalFormat("#,###.##")

        val remaining = budgetSum - totalAmountSum

        val remainingPercentage = if (budgetSum != 0.0) {
            (remaining / budgetSum) * 100
        } else {
            0.0
        }

        if (remainingPercentage > 0) {
            binding.progressBarCircularAll.progress = remainingPercentage.toInt()
            binding.progressTextAll.text = "${decimalFormat.format(remainingPercentage)}%"
        } else {
            binding.progressBarCircularAll.progress = 0
            binding.progressTextAll.text = "__"
        }

        binding.remainingTotalAllTv.text = decimalFormat.format(remaining)
        binding.totalCategoryAllTV.text = decimalFormat.format(totalAmountSum)
        binding.budgetTotalAll.text = decimalFormat.format(budgetSum)
    }
}