package com.example.myapplication.view.revenue_and_expenditure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.CategoryAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.FragmentIncomeBinding
import com.example.myapplication.view.category.SettingCategoryActivity
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory

class IncomeFragment : Fragment(), CategoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentIncomeBinding
    private lateinit var categoryAdapter: CategoryAdapter

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIncomeBinding.inflate(inflater, container, false)
        binding.recyclerViewIncome.layoutManager = GridLayoutManager(requireContext(), 4)

        categoryViewModel.allCategory.observe(viewLifecycleOwner, Observer { categoriesWithIcons ->
            categoriesWithIcons?.let { categories ->
                val filteredCategories = categories.filter { it.category.source == "Income" }
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

        return binding.root
    }

    private val settingsItem = CombinedCategoryIcon(
        categoryName = "Cài đặt",
        categoryType = "setting",
        iconResource = R.drawable.ic_add_24,
        iconType = "Special",
        source = "Special",
        idCategory = -1,
        icon = -1
    )

    private fun updateRecyclerView(combinedList: List<CombinedCategoryIcon>) {
        val reversedList = combinedList.asReversed().toMutableList()
        reversedList.add(settingsItem)
        categoryAdapter = CategoryAdapter(reversedList, this)
        binding.recyclerViewIncome.adapter = categoryAdapter
    }

    override fun onItemClick(category: CombinedCategoryIcon) {
        Log.d("IncomeFragment", "Item clicked: ${category.categoryName}")
    }

    override fun onSettingsClick() {
        val intent = Intent(requireContext(), SettingCategoryActivity::class.java)
        intent.putExtra("checkDisplay", 1)
        startActivity(intent)
    }

}