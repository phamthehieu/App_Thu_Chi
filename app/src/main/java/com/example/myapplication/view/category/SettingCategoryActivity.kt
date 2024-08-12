package com.example.myapplication.view.category

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerStettingAdapter
import com.example.myapplication.databinding.ActivitySettingCategoryBinding

class SettingCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingCategoryBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerStettingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPagerSetting
        adapter = ViewPagerStettingAdapter(this)
        viewPager.adapter = adapter

        binding.tabExpenseSetting.setOnClickListener {
            viewPager.currentItem = 0
        }

        binding.tabIncomeSetting.setOnClickListener {
            viewPager.currentItem = 1
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        val checkDisplay = intent.getIntExtra("checkDisplay", 0)
        viewPager.currentItem = checkDisplay
        updateTabBackground(checkDisplay + 1)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabBackground(position + 1)
            }
        })

    }

    private fun updateTabBackground(selectedTabNumber: Int) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
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
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.black1))
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
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
                binding.backBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
            }
        }
    }
}