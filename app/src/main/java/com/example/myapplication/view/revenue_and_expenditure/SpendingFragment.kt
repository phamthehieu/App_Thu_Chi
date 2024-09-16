package com.example.myapplication.view.revenue_and_expenditure

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.CategoryAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.FragmentSpendingBinding
import com.example.myapplication.utilities.OnCategorySelectedListener
import com.example.myapplication.view.component.KeyBoardBottomSheetFragment
import com.example.myapplication.view.category.SettingCategoryActivity
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import com.google.gson.Gson

class SpendingFragment : Fragment(), CategoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentSpendingBinding
    private lateinit var categoryAdapter: CategoryAdapter

    private var itemEdit: IncomeExpenseListData? = null

    private var dateSelected: String? = null

    private var listener: OnCategorySelectedListener? = null

    private var checkSearch: String? = null

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val json = it.getString("itemEdit")
            itemEdit = json?.let { jsonStr ->
                Gson().fromJson(jsonStr, IncomeExpenseListData::class.java)
            }
            val jsonSearch = it.getString("dataSearch")
            checkSearch = jsonSearch

            dateSelected = it.getString("dateSelected")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpendingBinding.inflate(inflater, container, false)
        binding.recyclerViewSpending.layoutManager = GridLayoutManager(requireContext(), 4)

        categoryViewModel.allCategory.observe(viewLifecycleOwner, Observer { categoriesWithIcons ->
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

        return binding.root
    }

    private val settingsItem = CombinedCategoryIcon(
        categoryName = "Cài đặt",
        categoryType = "setting",
        iconResource = R.drawable.ic_add_24,
        iconType = "Special",
        source = "Special",
        idCategory = -1,
        icon = -1,
        budget = "0"
    )

    private fun updateRecyclerView(combinedList: List<CombinedCategoryIcon>) {
        val reversedList = combinedList.asReversed().toMutableList()
        reversedList.add(settingsItem)

        val isMultiSelectEnabled = checkSearch == "dataSearch"

        categoryAdapter = CategoryAdapter(reversedList, object : CategoryAdapter.OnItemClickListener {
            override fun onItemClick(category: CombinedCategoryIcon) {
                this@SpendingFragment.onItemClick(category)
            }

            override fun onSettingsClick() {
                this@SpendingFragment.onSettingsClick()
            }
        }, isMultiSelectEnabled)

        binding.recyclerViewSpending.adapter = categoryAdapter

        itemEdit?.let { editItem ->
            val selectedIndex = reversedList.indexOfFirst { it.idCategory == editItem.categoryId }
            if (selectedIndex != -1) {
                binding.recyclerViewSpending.post {
                    binding.recyclerViewSpending.scrollToPosition(selectedIndex)
                    categoryAdapter.setEditItemPosition(selectedIndex)
                    categoryAdapter.notifyItemChanged(selectedIndex)
                    categoryAdapter.itemClickListener.onItemClick(reversedList[selectedIndex])
                }
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCategorySelectedListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onItemClick(category: CombinedCategoryIcon) {
        if (checkSearch == "dataSearch") {
            listener?.onCategorySelected(category)
        } else if (checkSearch == null) {
            val keyboard = KeyBoardBottomSheetFragment()
            keyboard.categoryData(category, itemEdit, dateSelected)
            keyboard.show(childFragmentManager, "keyboard")
        }
    }

    override fun onSettingsClick() {
        val intent = Intent(requireContext(), SettingCategoryActivity::class.java)
        intent.putExtra("checkDisplay", 0)
        startActivity(intent)
    }
}
