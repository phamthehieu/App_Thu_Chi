package com.example.myapplication.view.reports

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.SettingBudgetAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.ActivitySettingBudgetBinding
import com.example.myapplication.entity.Category
import com.example.myapplication.view.component.KeyBoardBottomSheetFragment
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class SettingBudgetActivity : AppCompatActivity(), SettingBudgetAdapter.OnItemClickListener {

    private lateinit var binding: ActivitySettingBudgetBinding

    private lateinit var settingCategoryActivity: SettingBudgetAdapter

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(this.application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        categoryViewModel.allCategory.observe(this, Observer { categoriesWithIcons ->
            categoriesWithIcons?.let { categories ->
                val filteredCategories = categories.filter { it.category.source == "Expense" }
                val combinedList = filteredCategories.map { categoryWithIcon ->
                    CombinedCategoryIcon(
                        categoryName = categoryWithIcon.category.name,
                        categoryType = categoryWithIcon.category.type,
                        iconResource = categoryWithIcon.icon.iconResource,
                        iconType = categoryWithIcon.icon.type,
                        source = categoryWithIcon.category.source,
                        idCategory = categoryWithIcon.category.id,
                        icon = categoryWithIcon.category.icon,
                        budget = categoryWithIcon.category.budget
                    )
                }
                updateRecyclerView(combinedList)
            }
        })
    }

    private fun updateRecyclerView(combinedList: List<CombinedCategoryIcon>) {
        val totalBudget = combinedList.sumOf {
            val cleanedBudget = it.budget.replace(",", ".").toDoubleOrNull() ?: 0.0
            cleanedBudget
        }
        if (totalBudget == "0.0".toDouble()) {
            binding.totalBudgetAll.text = "__"
        } else {
            val decimalFormat = DecimalFormat("#,###.##")
            binding.totalBudgetAll.text = decimalFormat.format(totalBudget).toString()
        }
        val reversedList = combinedList.asReversed().toMutableList()
        binding.recyclerViewCategory.layoutManager = LinearLayoutManager(this)
        settingCategoryActivity = SettingBudgetAdapter(reversedList, this)
        binding.recyclerViewCategory.adapter = settingCategoryActivity
    }

    override fun onItemClick(category: CombinedCategoryIcon) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_confirm_delete)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backDeleteBtn = dialog.findViewById<View>(R.id.backDeleteBtn)
        val successDeleteBtn = dialog.findViewById<View>(R.id.successDeleteBtn)

        backDeleteBtn.setOnClickListener {
            dialog.dismiss()
        }

        successDeleteBtn.setOnClickListener {
            val data = Category(
                id = category.idCategory,
                name = category.categoryName,
                type = category.categoryType,
                icon = category.icon,
                source = category.source,
                budget = "0"
            )
            categoryViewModel.viewModelScope.launch(Dispatchers.IO) {
                val insertedId = categoryViewModel.update(data)
                if (insertedId.toString() == "kotlin.Unit") {
                    runOnUiThread {
                        Toast.makeText(this@SettingBudgetActivity, "Xóa thành công", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun editBudget(category: CombinedCategoryIcon) {
        val keyboard = KeyBroadBottomReportsFragment()
        keyboard.setupDataCategory(category)
        keyboard.show(supportFragmentManager, "keyboard")
    }

}

