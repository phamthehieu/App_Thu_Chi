package com.example.myapplication.view.search

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerSearchAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.ActivityRevenueAndExpenditureSearchBinding
import com.example.myapplication.utilities.CategoryRepository
import com.example.myapplication.utilities.OnCategorySelectedListener

class RevenueAndExpenditureSearchActivity : AppCompatActivity(), OnCategorySelectedListener {

    private lateinit var binding: ActivityRevenueAndExpenditureSearchBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueAndExpenditureSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = findViewById(R.id.viewPagerSearchTv)

        binding.tabExpense.setOnClickListener {
            viewPager.currentItem = 0
        }

        binding.tabIncome.setOnClickListener {
            viewPager.currentItem = 1
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.confirmBtn.setOnClickListener {
            finish()
        }

        adapter = ViewPagerSearchAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabBackground(position + 1)
            }
        })
    }

    override fun onCategorySelected(category: CombinedCategoryIcon) {
        val selectedCategory = Pair(category.idCategory, category.categoryName)
        CategoryRepository.addCategory(selectedCategory)
    }

    private fun updateTabBackground(selectedTabNumber: Int) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val selectedColor = resources.getColor(R.color.black, this.theme)
        val unselectedColor = resources.getColor(R.color.white, this.theme)

        binding.tabExpense.apply {
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_white_left else 0)
                setTextColor(if (selectedTabNumber == 1) selectedColor else unselectedColor)
            } else {
                setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_black_left else 0)
                setTextColor(if (selectedTabNumber == 1) unselectedColor else selectedColor)
            }
        }
        binding.tabIncome.apply {
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_right else 0)
                setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
            } else {
                setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_black_right else 0)
                setTextColor(if (selectedTabNumber == 2) unselectedColor else selectedColor)
            }
        }
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100)
        } else {
            binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_black10_100)
        }
        binding.title.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.black1 else R.color.yellow
            )
        )
        binding.confirmBtn.setColorFilter(
            ContextCompat.getColor(
                this,
                if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.black1 else R.color.black1
            )
        )
        val color = ContextCompat.getColor(
            this,
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.black else R.color.yellow
        )
        this.window.statusBarColor = color
    }
}