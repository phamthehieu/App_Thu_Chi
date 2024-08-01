package com.example.myapplication.view.category

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.SettingCategoryAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.FragmentIncomeSettingBinding
import com.example.myapplication.entity.Category
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory

class IncomeSettingFragment : Fragment(), SettingCategoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentIncomeSettingBinding
    private lateinit var settingCategoryActivity: SettingCategoryAdapter

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
        binding = FragmentIncomeSettingBinding.inflate(inflater, container, false)
        binding.recyclerViewIncomeSetting.layoutManager = LinearLayoutManager(requireContext())

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
                        icon = categoryWithIcon.category.icon,
                        budget = categoryWithIcon.category.budget
                    )
                }
                updateRecyclerView(combinedList)
            }
        })

        binding.addCategoryIncomeBtn.setOnClickListener {
            val intent = Intent(requireContext(), AddNewCategoryActivity::class.java)
            intent.putExtra("checkDisplay", 2)
            intent.putExtra("type", "add")
            startActivity(intent)
        }

        return binding.root
    }

    private fun updateRecyclerView(combinedList: List<CombinedCategoryIcon>) {
        val reversedList = combinedList.asReversed().toMutableList()
        settingCategoryActivity = SettingCategoryAdapter(reversedList,this)
        binding.recyclerViewIncomeSetting.adapter = settingCategoryActivity
    }

    override fun onItemClick(category: CombinedCategoryIcon) {
        context.let { cxt ->
            val dialog = Dialog(cxt!!)
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
                    budget = category.budget
                )
                categoryViewModel.delete(data)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun editCategory(category: CombinedCategoryIcon) {
        val intent = Intent(requireContext(), AddNewCategoryActivity::class.java).apply {
            putExtra("checkDisplay", 2)
            putExtra("type", "edit")
            putExtra("Category", category)
        }
        startActivity(intent)
    }

}