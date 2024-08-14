package com.example.myapplication.view.account

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.IncomeExpenseListAdapter
import com.example.myapplication.adapter.MonthPagerAdapter
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityAccountDetailsBinding
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.example.myapplication.utilities.DeleteDialogUtils
import com.example.myapplication.utilities.convertToIncomeExpenseListData
import com.example.myapplication.view.home.DetailActivity
import com.example.myapplication.view.revenue_and_expenditure.RevenueAndExpenditureActivity
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Calendar

class AccountDetailsActivity : AppCompatActivity(), IncomeExpenseListAdapter.OnItemClickListener,
    KeyBroadBottomSheetAccountFragment.OnNumberSequenceListener {

    private lateinit var binding: ActivityAccountDetailsBinding

    companion object {
        private const val REQUEST_CODE_EDIT = 1
    }

    private var dataAccount: AccountIconFormat? = null

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var check = true

    private lateinit var adapter: IncomeExpenseListAdapter

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = intent.getStringExtra("dataAccount")
        dataAccount = json?.let {
            Gson().fromJson(it, AccountIconFormat::class.java)
        }

        setupViews()

        setupRecyclerView()

        setupNightMode()
    }


    @SuppressLint("DefaultLocale")
    private fun setupRecyclerView() {
        val formattedMonth = String.format("%02d", monthSearch)
        incomeExpenseListModel.getListIncomeExpenseListByAccountYear(
            dataAccount?.id.toString(),
            yearSearch.toString(),
            formattedMonth
        ).observe(this) { data ->
            val incomeExpenseList = data.reversed().map { convertToIncomeExpenseListData(it) }
            val totalIncome = incomeExpenseList.filter { it.type == "Income" }
                .sumOf { it.amount.replace(",", ".").toDouble() }
            val totalExpense = incomeExpenseList.filter { it.type == "Expense" }
                .sumOf { it.amount.replace(",", ".").toDouble() }
            val expenseFormatter = DecimalFormat("#,###.##")
            val formattedExpense = expenseFormatter.format(totalExpense)
            val formattedIncome = expenseFormatter.format(totalIncome)

            binding.expenseAccountTV.text = formattedExpense
            binding.incomeAccountTV.text = formattedIncome

            val groupedIncomeExpenseList = groupIconsByType(incomeExpenseList)

            binding.recyclerViewAccountDetail.layoutManager = LinearLayoutManager(this)

            adapter = IncomeExpenseListAdapter(groupedIncomeExpenseList, this)
            binding.recyclerViewAccountDetail.adapter = adapter
        }
    }

    private fun groupIconsByType(data: List<IncomeExpenseListData>): Map<String, List<IncomeExpenseListData>> {
        return data.groupBy { it.date }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
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

        binding.editAmountAccount.setOnClickListener {
            val keyboardBottomSheet = KeyBroadBottomSheetAccountFragment()
            val bundle = Bundle()
            val normalizedNumberSequence = dataAccount?.amountAccount?.replace(',', '.')
            val number = normalizedNumberSequence?.toDouble()
            if (number != null) {
                bundle.putDouble("amountAccount", number)
            }
            keyboardBottomSheet.arguments = bundle
            keyboardBottomSheet.show(supportFragmentManager, keyboardBottomSheet.tag)
        }

        binding.detailAccountBtn.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun showPopupMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.account_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.account_edit -> {
                    val intent = Intent(this, AddNewAccountActivity::class.java).apply {
                        putExtra("type", "edit")
                        putExtra("Account", dataAccount!!)
                    }
                    startActivityForResult(intent, REQUEST_CODE_EDIT)
                    true
                }

                R.id.account_delete -> {
                    DeleteDialogUtils.showDeleteDialog(
                        this,
                        dataAccount!!,
                        accountTypeViewModel
                    ) {
                        finish()
                    }
                    true
                }

                else -> false
            }
        }

        popupMenu.show()

    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialogBox() {
        val months = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 50..currentYear + 50).toList()
        val dialog = Dialog(this)

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
            monthSearch - 1,
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
                    monthSearch - 1,
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
                leftBtn.visibility = View.GONE
                rightBtn.visibility = View.GONE
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
                    monthSearch - 1,
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
                leftBtn.visibility = View.VISIBLE
                rightBtn.visibility = View.VISIBLE
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
            setupRecyclerView()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onItemClick(incomeExpense: Any) {
        val gson = Gson()
        val json = gson.toJson(incomeExpense)
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("itemDetail", json)
        startActivity(intent)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = this.menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                val selectedItem = adapter.getSelectedItem()
                selectedItem?.let { itemToEdit ->
                    val gson = Gson()
                    val json = gson.toJson(itemToEdit)
                    val intent = Intent(this, RevenueAndExpenditureActivity::class.java)
                    intent.putExtra("itemToEdit", json)
                    startActivity(intent)
                }
                true
            }

            R.id.action_delete -> {
                conformDelete()
                true
            }

            R.id.action_detail -> {
                val selectedItem = adapter.getSelectedItem()
                selectedItem?.let { itemToEdit ->
                    val gson = Gson()
                    val json = gson.toJson(itemToEdit)
                    val intent = Intent(this, DetailActivity::class.java)
                    intent.putExtra("itemDetail", json)
                    startActivity(intent)
                }

                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun conformDelete() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_confirm_delete)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backDeleteBtn = dialog.findViewById<View>(R.id.backDeleteBtn)
        val successDeleteBtn = dialog.findViewById<View>(R.id.successDeleteBtn)

        backDeleteBtn.setOnClickListener {
            dialog.dismiss()
        }

        successDeleteBtn.setOnClickListener {
            val selectedItem = adapter.getSelectedItem()
            if (selectedItem is IncomeExpenseListData) {
                selectedItem.let { itemData ->
                    val itemToDelete = IncomeExpenseList(
                        id = itemData.id,
                        note = itemData.note,
                        amount = itemData.amount,
                        date = itemData.date,
                        categoryId = itemData.categoryId,
                        type = itemData.type,
                        image = itemData.image,
                        categoryName = itemData.categoryName,
                        iconResource = itemData.iconResource,
                        accountId = itemData.accountId
                    )
                    GlobalScope.launch {
                        incomeExpenseListModel.deleteIncomeExpenseListModel(itemToDelete)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AccountDetailsActivity,
                                "Đã xóa thành công",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                    }
                }
            } else if (selectedItem is HistoryAccount) {
                // Xử lý HistoryAccount
            }
        }

        dialog.show()
    }

    override fun onNumberSequenceEntered(numberSequence: String) {
        dataAccount?.amountAccount = numberSequence
        setupViews()
        val data = Account(
            id = dataAccount!!.id,
            nameAccount = dataAccount!!.nameAccount,
            typeAccount = dataAccount!!.typeAccount,
            amountAccount = numberSequence,
            icon = dataAccount!!.icon,
            note = dataAccount!!.note
        )
        accountTypeViewModel.updateAccount(data).observe(this) { result ->
            if (result) {
                Toast.makeText(this, "Sửa tài khoản thành công", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Sửa tài khoản thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT && resultCode == Activity.RESULT_OK) {
            val updatedAccount = data?.getParcelableExtra<AccountIconFormat>("updatedAccount")
            if (updatedAccount != null) {
                dataAccount = updatedAccount
                setupViews()
                setupRecyclerView()
            }
        }
    }

    private fun setupNightMode() {
        val currentNightMode =
            this.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                binding.accountTotal.setBackgroundResource(R.drawable.bottom_sheet_border_yellow)
                binding.backBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.detailAccountBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.iconEdit.setColorFilter(ContextCompat.getColor(this, R.color.black))
            }

            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                binding.accountTotal.setBackgroundResource(R.drawable.bottom_sheet_border_gray)
                binding.backBtn.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.detailAccountBtn.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.iconEdit.setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
        }
    }


}