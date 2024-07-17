package com.example.myapplication.view.revenue_and_expenditure

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityRevenueAndExpenditureBinding
import com.google.gson.Gson

class RevenueAndExpenditureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRevenueAndExpenditureBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter
    private var itemEdit: IncomeExpenseListData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueAndExpenditureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = intent.getStringExtra("itemToEdit")
        itemEdit = json?.let {
            Gson().fromJson(it, IncomeExpenseListData::class.java)
        }

        viewPager = findViewById(R.id.viewPagerTv)
        adapter = ViewPagerAdapter(this, itemEdit)
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

        itemEdit?.let {
            if (it.type == "Income") {
                viewPager.setCurrentItem(1, false) // Chuyển trực tiếp đến IncomeFragment
                updateTabBackground(2)
            } else {
                viewPager.currentItem = 0
                updateTabBackground(1)
            }
        } ?: updateTabBackground(1)
    }

    private fun updateTabBackground(selectedTabNumber: Int) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val selectedColor = ContextCompat.getColor(this, if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.black else R.color.white)
        val unselectedColor = ContextCompat.getColor(this, if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.white else R.color.black)

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
        binding.title.setBackgroundColor(ContextCompat.getColor(this, if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.black1 else R.color.yellow))
        binding.monthlyPayment.setColorFilter(ContextCompat.getColor(this, if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.black1 else R.color.black1))
        val color = ContextCompat.getColor(this, if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.black else R.color.yellow)
        this.window.statusBarColor = color
    }
}
