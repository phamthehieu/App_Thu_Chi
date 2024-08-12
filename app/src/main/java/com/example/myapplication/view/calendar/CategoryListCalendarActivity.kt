package com.example.myapplication.view.calendar

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.CategoryListAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityCategoryListCalendarBinding
import com.example.myapplication.entity.Category
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.view.revenue_and_expenditure.RevenueAndExpenditureActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class CategoryListCalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListCalendarBinding
    private val categoryListAdapter = CategoryListAdapter()
    private var date: LocalDate? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryListCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackground()

        setupRecyclerView()

        setupNightMode()
    }

    private fun setupNightMode() {
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.exFiveAppBarLayout.setBackgroundResource(R.color.yellow)
                binding.backBtn.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        val json = intent.getStringExtra("listCategory")
        val dateSelectedString = intent.getStringExtra("dateSelected")

        if (json != null) {
            try {
                val gson = Gson()
                val type = object : TypeToken<List<IncomeExpenseList>>() {}.type
                val listCategory: List<IncomeExpenseList> = gson.fromJson(json, type)
                categoryListAdapter.submitList(listCategory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            categoryListAdapter.submitList(emptyList())
        }

        if (dateSelectedString != null) {
            try {
                val dateSelected: LocalDate = LocalDate.parse(dateSelectedString)
                date = dateSelected
                binding.exMonthYearText.text = " ${dateSelected.dayOfMonth} thg ${dateSelected.monthValue}, ${dateSelected.year}"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupBackground() {
        binding.exFiveRv.apply {
            layoutManager = LinearLayoutManager(this@CategoryListCalendarActivity, RecyclerView.VERTICAL, false)
            adapter = categoryListAdapter
        }

        binding.backBtn.setOnClickListener {
            finish()

        }

        binding.fabBtn.setColorFilter(R.color.black)

        binding.fabBtn.setOnClickListener {
            val intent = Intent(this, RevenueAndExpenditureActivity::class.java)
            intent.putExtra("dateSelected", date.toString())
            startActivity(intent)
        }
    }
}
