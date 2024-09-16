package com.example.myapplication.view.revenue_and_expenditure

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityRevenueAndExpenditureBinding
import com.google.gson.Gson
import java.time.LocalDate

class RevenueAndExpenditureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRevenueAndExpenditureBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter

    private var itemEdit: IncomeExpenseListData? = null
    private var dateSelected: LocalDate? = null
    private var itemAccount: HistoryAccountWithAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueAndExpenditureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = intent.getStringExtra("itemToEdit")
        itemEdit = json?.let {
            Gson().fromJson(it, IncomeExpenseListData::class.java)
        }

        val jsonAccount = intent.getStringExtra("itemToEditAccount")
        itemAccount = jsonAccount?.let {
            Gson().fromJson(it, HistoryAccountWithAccount::class.java)
        }

        viewPager = findViewById(R.id.viewPagerTv)

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

        val dateSelectedString = intent.getStringExtra("dateSelected")

        if (dateSelectedString != null) {
            dateSelected = LocalDate.parse(dateSelectedString)
        }

        adapter = ViewPagerAdapter(this, itemEdit, itemAccount, dateSelected)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabBackground(position + 1)
            }
        })

        if (itemEdit != null) {
            itemEdit?.let {
                when (it.type) {
                    "Expense" -> {
                        viewPager.currentItem = 0
                        updateTabBackground(1)
                    }

                    "Income" -> {
                        viewPager.currentItem = 1
                        updateTabBackground(2)
                    }
                }
            } ?: updateTabBackground(1)
        } else if (itemAccount != null) {
            viewPager.currentItem = 2
            updateTabBackground(3)
        }
    }

    private fun updateTabBackground(selectedTabNumber: Int) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val selectedColor = resources.getColor(R.color.black, this.theme)
        val unselectedColor = resources.getColor(R.color.white, this.theme)

        when (selectedTabNumber) {
            1 -> {
                binding.leftView.visibility = View.GONE
                binding.rightView.visibility = View.VISIBLE
            }

            2 -> {
                binding.leftView.visibility = View.GONE
                binding.rightView.visibility = View.GONE
            }

            3 -> {
                binding.leftView.visibility = View.VISIBLE
                binding.rightView.visibility = View.GONE
            }
        }

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
                setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_white_center else 0)
                setTextColor(if (selectedTabNumber == 2) selectedColor else unselectedColor)
            } else {
                setBackgroundResource(if (selectedTabNumber == 2) R.drawable.round_back_black_center else 0)
                setTextColor(if (selectedTabNumber == 2) unselectedColor else selectedColor)
            }
        }
        binding.tabTransfer.apply {
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                setBackgroundResource(if (selectedTabNumber == 3) R.drawable.round_back_white_right else 0)
                setTextColor(if (selectedTabNumber == 3) selectedColor else unselectedColor)
            } else {
                setBackgroundResource(if (selectedTabNumber == 3) R.drawable.round_back_black_right else 0)
                setTextColor(if (selectedTabNumber == 3) unselectedColor else selectedColor)
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
        binding.monthlyPayment.setColorFilter(
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
