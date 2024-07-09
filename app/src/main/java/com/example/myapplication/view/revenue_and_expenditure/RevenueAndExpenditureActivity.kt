package com.example.myapplication.view.revenue_and_expenditure

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.databinding.ActivityRevenueAndExpenditureBinding

class RevenueAndExpenditureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRevenueAndExpenditureBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueAndExpenditureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = findViewById(R.id.viewPagerTv)
        adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        binding.tabExpense.setOnClickListener {
            viewPager.currentItem = 0
        }

        binding.tabIncome.setOnClickListener {
            viewPager.currentItem = 1
        }

        binding.tabTransfer.setOnClickListener {
            viewPager.currentItem = 2
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabBackground(position + 1)
            }
        })

        updateTabBackground(1)
    }

    private fun updateTabBackground(selectedTabNumber: Int) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val selectedColor = resources.getColor(R.color.black, theme)
                val unselectedColor = resources.getColor(R.color.white, theme)

                binding.tabExpense.apply {
                    setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_white_left else 0)
                    setTextColor(if (selectedTabNumber == 1) selectedColor else unselectedColor)
                }
                binding.tabIncome.apply {
                    setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_center else 0)
                    setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
                }
                binding.tabTransfer.apply {
                    setBackgroundResource(if (selectedTabNumber == 3) R.drawable.round_back_white_right else 0)
                    setTextColor(if (selectedTabNumber == 3) selectedColor else unselectedColor)
                }
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100)
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.black1))
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                val selectedColor = resources.getColor(R.color.white, theme)
                val unselectedColor = resources.getColor(R.color.black, theme)

                binding.tabExpense.apply {
                    setBackgroundResource(if (selectedTabNumber == 1) R.drawable.round_back_white_left_black else 0)
                    setTextColor(if (selectedTabNumber == 1) selectedColor else unselectedColor)
                }
                binding.tabIncome.apply {
                    setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_center_black else 0)
                    setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
                }
                binding.tabTransfer.apply {
                    setBackgroundResource(if (selectedTabNumber == 3) R.drawable.round_back_white_right_black else 0)
                    setTextColor(if (selectedTabNumber == 3) selectedColor else unselectedColor)
                }
                binding.titleSubAAE.setBackgroundResource(R.drawable.round_back_white10_100_black)
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                binding.monthlyPayment.setColorFilter(ContextCompat.getColor(this, R.color.black1))
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
            }
        }
    }
}
