package com.example.myapplication.view.category

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.IconListAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.ActivityAddNewCategoryBinding
import com.example.myapplication.entity.Category
import com.example.myapplication.entity.Icon
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import com.example.myapplication.viewModel.IconViewModel
import com.example.myapplication.viewModel.IconViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddNewCategoryActivity : AppCompatActivity(), IconListAdapter.OnIconSelectedListener {

    private lateinit var binding: ActivityAddNewCategoryBinding
    private lateinit var adapter: IconListAdapter
    private var iconSelected: Icon? = null

    private val iconViewModel: IconViewModel by viewModels {
        IconViewModelFactory(application)
    }

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = GridLayoutManager(this, 5)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == IconListAdapter.TYPE_HEADER) {
                    5
                } else {
                    1
                }
            }
        }

        var method = 0

        binding.recyclerViewIconAddCategory.layoutManager = layoutManager

        binding.imageIconSelected.setColorFilter(Color.BLACK)

        binding.tabExpenseSetting.setOnClickListener {
            method = 1
            updateTabBackground(1)
        }

        binding.tabIncomeSetting.setOnClickListener {
            method = 2
            updateTabBackground(2)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        iconViewModel.allIcons.observe(this, Observer { icons ->
            val groupedIcons = groupIconsByType(icons)
            val flatIconsList = groupedIcons.values.flatten()

            val checkType = intent.getStringExtra("type")
            if (checkType == "edit") {
                val category = intent.getParcelableExtra<CombinedCategoryIcon>("Category")
                if (category != null) {
                    val position = flatIconsList.indexOfFirst { it.id == category.icon }
                    val groups = groupedIcons.values.toList()
                    var groupIndex = 0
                    var iconIndex = 0
                    var iconsCounted = 0

                    for ((index, group) in groups.withIndex()) {
                        iconsCounted += group.size
                        if (position < iconsCounted) {
                            groupIndex = index
                            iconIndex = position - (iconsCounted - group.size)
                            break
                        }
                    }
                    val groupsPassed = groupIndex

                    adapter = IconListAdapter(groupedIcons, this, (position + groupsPassed + 1))
                    binding.imageIconSelected.setImageResource(category.iconResource)
                    binding.nameCategoryEt.setText(category.categoryName)
                    binding.recyclerViewIconAddCategory.adapter = adapter
                }
            } else {
                adapter = IconListAdapter(groupedIcons, this, 1)
                binding.recyclerViewIconAddCategory.adapter = adapter
            }
        })

        val checkDisplay = intent.getIntExtra("checkDisplay", 0)

        method = checkDisplay
        updateTabBackground(checkDisplay)

        binding.addNewCategory.setOnClickListener {
            addNewCategory(method)
        }

    }

    private fun addNewCategory(method: Int) {
        val nameCategory = binding.nameCategoryEt.text.toString()
        if (nameCategory.isNotEmpty()) {
            var source = ""
            if (method == 1) {
                source = "Expense"
            } else if (method == 2) {
                source = "Income"
            }
            val checkType = intent.getStringExtra("type")
            if (checkType == "edit") {
                val category = intent.getParcelableExtra<CombinedCategoryIcon>("Category")
                val categoryNew = iconSelected?.let {
                    category?.let { it1 ->
                        Category(
                            id = it1.idCategory,
                            name = nameCategory,
                            icon = it.id,
                            type = "user",
                            source = source,
                            budget = it1.budget
                        )
                    }
                }
                categoryNew?.let {
                    categoryViewModel.viewModelScope.launch(Dispatchers.IO) {
                        val insertedId = categoryViewModel.update(it)
                        if (insertedId.toString() == "kotlin.Unit") {
                            finish()
                        }
                    }
                }
            } else {
                val categoryNew = iconSelected?.let {
                    Category(
                        name = nameCategory,
                        icon = it.id,
                        type = "user",
                        source = source,
                        budget = "0"
                    )
                }
                categoryNew?.let {
                    categoryViewModel.viewModelScope.launch(Dispatchers.IO) {
                        val insertedId = categoryViewModel.insert(it)
                        if (insertedId.toString() == "kotlin.Unit") {
                            finish()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(this, "Tên danh mục không được để trống", Toast.LENGTH_SHORT).show()
        }
    }

    private fun groupIconsByType(icons: List<Icon>): Map<String, List<Icon>> {
        return icons.groupBy { it.type }
    }

    private fun updateTabBackground(selectedTabNumber: Int) {
        val currentNightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val selectedColor = resources.getColor(R.color.black, theme)
                val unselectedColor = resources.getColor(R.color.white, theme)

                binding.tabExpenseSetting.apply {
                    setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_white_left else 0)
                    setTextColor(if (selectedTabNumber == 1) selectedColor else unselectedColor)
                }
                binding.tabIncomeSetting.apply {
                    setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_right else 0)
                    setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
                }
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100)
                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black1
                    )
                )
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                val selectedColor = resources.getColor(R.color.white, theme)
                val unselectedColor = resources.getColor(R.color.black, theme)

                binding.tabExpenseSetting.apply {
                    setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_white_left_black else 0)
                    setTextColor(if (selectedTabNumber == 1) selectedColor else unselectedColor)
                }
                binding.tabIncomeSetting.apply {
                    setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_right_black else 0)
                    setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
                }
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100_black)
                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.yellow
                    )
                )
                binding.addNewCategory.setColorFilter(Color.BLACK)
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
            }
        }
    }

    override fun onIconSelected(icon: Icon) {
        iconSelected = icon
        binding.imageIconSelected.setImageResource(icon.iconResource)
    }
}