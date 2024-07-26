package com.example.myapplication.view.reports

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.SettingBudgetAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.ActivitySettingBudgetBinding
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory

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
                        icon = categoryWithIcon.category.icon
                    )
                }
                updateRecyclerView(combinedList)
            }
        })
    }

    private fun updateRecyclerView(combinedList: List<CombinedCategoryIcon>) {
        val reversedList = combinedList.asReversed().toMutableList()
        binding.recyclerViewCategory.layoutManager = LinearLayoutManager(this)
        settingCategoryActivity = SettingBudgetAdapter(reversedList, this)
        binding.recyclerViewCategory.adapter = settingCategoryActivity
    }

    override fun onItemClick(category: CombinedCategoryIcon) {
        TODO("Not yet implemented")
    }

    override fun editBudget(category: CombinedCategoryIcon) {
        TODO("Not yet implemented")
    }

}

