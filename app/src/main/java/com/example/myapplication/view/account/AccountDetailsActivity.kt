package com.example.myapplication.view.account

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.MonthPagerAdapter
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityAccountDetailsBinding
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.google.gson.Gson
import java.text.DecimalFormat
import java.util.Calendar

class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailsBinding

    private var dataAccount: AccountIconFormat? = null

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var check = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = intent.getStringExtra("dataAccount")
        dataAccount = json?.let {
            Gson().fromJson(it, AccountIconFormat::class.java)
        }

        setupViews()
    }


    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        val expenseFormatter = DecimalFormat("#,###.##")
        val amount = dataAccount?.amountAccount?.replace(",", ".")?.toDouble()

        binding.monthTv.text = "Thg $monthSearch $yearSearch"

        binding.nameAccountDetailTV.text = dataAccount?.nameAccount
        binding.noteAccountDetailTV.text = dataAccount?.note
        binding.amountAccountDetailTV.text = expenseFormatter.format(amount)
        binding.imageAccountDetailIV.setImageResource(dataAccount?.iconResource ?: 0)
        binding.imageAccountDetailIV.setColorFilter(ContextCompat.getColor(this, R.color.black))

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.popupCalenderBtn.setOnClickListener {
            showCustomDialogBox()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialogBox() {
        val months = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val years = (currentYear - 50..currentYear + 50).toList()
        val dialog = Dialog(this)
        var yearData = Calendar.getInstance().get(Calendar.YEAR)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.fragment_pop_up_calender)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backBtn: TextView = dialog.findViewById(R.id.backBtn)
        val yearTv: LinearLayout = dialog.findViewById(R.id.yearTv)
        val yearIcon: ImageButton = dialog.findViewById(R.id.yearIcon)
        val textYearTitle: TextView = dialog.findViewById(R.id.textYearTitle)
        val titleRecord: TextView = dialog.findViewById(R.id.titleRecord)
        val leftBtn: ImageButton = dialog.findViewById(R.id.leftBtn)
        val rightBtn: ImageButton = dialog.findViewById(R.id.rightBtn)
        val successBtn: TextView = dialog.findViewById(R.id.successBtn)

        fun updateTitleRecord(month: Int, year: Int) {
            titleRecord.text = "tháng $month năm $year"
            textYearTitle.text = year.toString()
        }
        updateTitleRecord(monthSearch, yearSearch)

        val viewPager: ViewPager2 = dialog.findViewById(R.id.viewPager)
        var monthPagerAdapter = MonthPagerAdapter(
            this,
            months,
            false,
            yearSearch,
            currentMonth,
            object : OnMonthSelectedListener {
                override fun onMonthSelected(month: Int) {
                    if (!check) {
                        yearSearch = month
                    } else {
                        monthSearch = month + 1
                    }
                    updateTitleRecord(monthSearch, yearSearch)
                }
            }
        )
        viewPager.adapter = monthPagerAdapter
        viewPager.currentItem = 50
        viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        viewPager.currentItem = years.indexOf(currentYear)
        yearIcon.setBackgroundResource(R.drawable.ic_sort_down_24)

        leftBtn.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        rightBtn.setOnClickListener {
            if (viewPager.currentItem < (viewPager.adapter?.itemCount ?: (0 - 1))) {
                viewPager.currentItem += 1
            }
        }

        yearTv.setOnClickListener {
            if (check) {
                monthPagerAdapter = MonthPagerAdapter(
                    this,
                    years,
                    true,
                    yearSearch,
                    currentMonth,
                    object : OnMonthSelectedListener {
                        override fun onMonthSelected(month: Int) {
                            if (!check) {
                                yearSearch = month
                            } else {
                                monthSearch = month + 1
                            }
                            updateTitleRecord(monthSearch, yearSearch)
                        }
                    }
                )
                viewPager.adapter = monthPagerAdapter
                viewPager.offscreenPageLimit = 1
                viewPager.isUserInputEnabled = false
                yearIcon.setBackgroundResource(R.drawable.ic_sort_up_24)
                check = false
            } else {
                monthPagerAdapter = MonthPagerAdapter(
                    this,
                    months,
                    false,
                    yearSearch,
                    currentMonth,
                    object : OnMonthSelectedListener {
                        override fun onMonthSelected(month: Int) {
                            if (!check) {
                                yearSearch = month
                            } else {
                                monthSearch = month + 1
                            }
                            updateTitleRecord(monthSearch, yearSearch)
                        }
                    }
                )
                viewPager.adapter = monthPagerAdapter
                viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                viewPager.isUserInputEnabled = true
                yearIcon.setBackgroundResource(R.drawable.ic_sort_down_24)
                check = true
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position != 0) {
                    yearSearch = years[position]
                    textYearTitle.text = years[position].toString()
                    updateTitleRecord(monthSearch, yearSearch)
                    monthPagerAdapter.updateYearData(yearSearch)
                }
            }
        })

        backBtn.setOnClickListener {
            dialog.dismiss()
        }
        successBtn.setOnClickListener {
            binding.monthTv.text = "Thg $monthSearch $yearSearch"
//            setupData()
            dialog.dismiss()
        }
        dialog.show()
    }
}