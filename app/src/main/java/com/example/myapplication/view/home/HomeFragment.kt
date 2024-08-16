package com.example.myapplication.view.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.adapter.IncomeExpenseListAdapter
import com.example.myapplication.adapter.MonthPagerAdapter
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.example.myapplication.utilities.convertToIncomeExpenseListData
import com.example.myapplication.view.calendar.CalendarHomeActivity
import com.example.myapplication.view.revenue_and_expenditure.RevenueAndExpenditureActivity
import com.example.myapplication.view.search.SearchActivity
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.HistoryAccountViewModel
import com.example.myapplication.viewModel.HistoryAccountViewModelFactory
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

class HomeFragment : Fragment(), OnMonthSelectedListener,
    IncomeExpenseListAdapter.OnItemClickListener {

    private lateinit var binding: FragmentHomeBinding

    private var monthSearch = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var yearSearch = Calendar.getInstance().get(Calendar.YEAR)

    private var check = true
    private lateinit var adapter: IncomeExpenseListAdapter

    private var selectedItemPosition: Int? = null

    private var checkMode = true

    private var account1: Account? = null

    private var account2: Account? = null

    private val combinedData = MediatorLiveData<List<Any>>()

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(requireActivity().application)
    }

    private val accountViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(requireActivity().application)
    }

    private val historyAccountViewModel: HistoryAccountViewModel by viewModels {
        HistoryAccountViewModelFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        combineLiveDataSources()
    }

    override fun onMonthSelected(month: Int) {
        if (!check) {
            yearSearch = month
        } else {
            monthSearch = month + 1
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.popupCalenderBtn.setOnClickListener {
            showCustomDialogBox()
        }

        binding.searchBtn.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        binding.calendarBtn.setOnClickListener {
            val intent = Intent(requireContext(), CalendarHomeActivity::class.java)
            startActivity(intent)
        }

        binding.monthTv.text = "Thg $monthSearch"

        binding.yearTv.text = yearSearch.toString()

        binding.recyclerViewHome.layoutManager = LinearLayoutManager(requireContext())

        setupBackground()

        setupNightMode()

        return binding.root;
    }

    @SuppressLint("DefaultLocale")
    private fun combineLiveDataSources() {
        val formattedMonth = String.format("%02d", monthSearch)
        val allHistoryAccountData = historyAccountViewModel.allHistoryAccount
        val incomeExpenseData = incomeExpenseListModel.getIncomeExpenseListByMonthYear(
            yearSearch.toString(), formattedMonth
        )

        combinedData.addSource(allHistoryAccountData) { historyAccounts ->
            val incomeExpenseList =
                incomeExpenseData.value?.map { convertToIncomeExpenseListData(it) } ?: emptyList()
            combinedData.value = combineDataSources(historyAccounts, incomeExpenseList)
        }

        combinedData.addSource(incomeExpenseData) { incomeExpenseList ->
            val historyAccounts = allHistoryAccountData.value ?: emptyList()
            val convertedIncomeExpenseList =
                incomeExpenseList.map { convertToIncomeExpenseListData(it) }
            combinedData.value = combineDataSources(historyAccounts, convertedIncomeExpenseList)
        }
    }

    private fun combineDataSources(
        historyAccounts: List<HistoryAccountWithAccount>,
        incomeExpenseList: List<IncomeExpenseListData>
    ): List<Any> {
        return historyAccounts + incomeExpenseList
    }

    @SuppressLint("DefaultLocale")
    private fun setupBackground() {
        combinedData.observe(viewLifecycleOwner) { combinedList ->
            val incomeExpenseList = combinedList.filterIsInstance<IncomeExpenseListData>()
            val historyAccountList = combinedList.filterIsInstance<HistoryAccountWithAccount>()
            val totalIncome = incomeExpenseList.filter { it.type == "Income" }
                .sumOf { it.amount.replace(",", ".").toDouble() }
            val totalExpense = incomeExpenseList.filter { it.type == "Expense" }
                .sumOf { it.amount.replace(",", ".").toDouble() }

            val expenseFormatter = DecimalFormat("#,###.##")
            binding.costTv.text = expenseFormatter.format(totalIncome)
            binding.IncomeTv.text = expenseFormatter.format(totalExpense)
            binding.totalBalanceTv.text = expenseFormatter.format(totalIncome - totalExpense)

            val combinedForAdapter = (incomeExpenseList + historyAccountList).let { list ->
                groupIconsByType(list)
            }

            adapter = IncomeExpenseListAdapter(combinedForAdapter, this)
            binding.recyclerViewHome.adapter = adapter

            val swipeGesture = object : SwipeGesture(requireContext()) {
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return if (viewHolder is IncomeExpenseListAdapter.HeaderViewHolder) {
                        makeMovementFlags(0, 0)
                    } else {
                        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                        makeMovementFlags(0, swipeFlags)
                    }
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (viewHolder is IncomeExpenseListAdapter.HeaderViewHolder) {
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                        return
                    }

                    val position = viewHolder.adapterPosition
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            selectedItemPosition = position
                            adapter.removeItem(position)
                            conformDelete()
                        }

                        ItemTouchHelper.RIGHT -> {
                            selectedItemPosition = position
                            adapter.removeItem(position)
                            val selectedItem = adapter.getSelectedItem()
                            selectedItem?.let {
                                val gson = Gson()
                                val json = gson.toJson(it)
                                val intent = Intent(
                                    requireContext(),
                                    RevenueAndExpenditureActivity::class.java
                                )
                                intent.putExtra("itemToEdit", json)
                                startActivity(intent)
                                adapter.notifyItemChanged(selectedItemPosition ?: return@let)
                            }
                        }
                    }
                }
            }

            val itemTouchHelper = ItemTouchHelper(swipeGesture)
            itemTouchHelper.attachToRecyclerView(binding.recyclerViewHome)
        }
    }

    private fun groupIconsByType(data: List<Any>): Map<String, List<Any>> {
        return data.groupBy { item ->
            when (item) {
                is IncomeExpenseListData -> item.date
                is HistoryAccountWithAccount -> item.historyAccount.date
                else -> throw IllegalArgumentException("Unsupported data type")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialogBox() {
        context?.let { ctx ->
            val months = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val years = (currentYear - 50..currentYear + 50).toList()
            val dialog = Dialog(ctx)

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

            if (checkMode) {
                leftBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                rightBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                leftBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
                rightBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
            }

            fun updateTitleRecord(month: Int, year: Int) {
                titleRecord.text = "tháng $month năm $year"
                textYearTitle.text = year.toString()
            }
            updateTitleRecord(monthSearch, yearSearch)

            val viewPager: ViewPager2 = dialog.findViewById(R.id.viewPager)
            var monthPagerAdapter = MonthPagerAdapter(
                requireContext(),
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
                        requireContext(),
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
                    viewPager.adapter = monthPagerAdapter
                    viewPager.offscreenPageLimit = 1
                    viewPager.isUserInputEnabled = false
                    yearIcon.setBackgroundResource(R.drawable.ic_sort_up_24)
                    check = false
                } else {
                    monthPagerAdapter = MonthPagerAdapter(
                        requireContext(),
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
                binding.yearTv.text = yearSearch.toString()
                binding.monthTv.text = "Thg $monthSearch"
                setupBackground()
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                val selectedItem = adapter.getSelectedItem()
                if (selectedItem is IncomeExpenseListData) {
                    selectedItem.let { itemToEdit ->
                        val gson = Gson()
                        val json = gson.toJson(itemToEdit)
                        val intent = Intent(requireContext(), RevenueAndExpenditureActivity::class.java)
                        intent.putExtra("itemToEdit", json)
                        startActivity(intent)
                    }
                } else if (selectedItem is HistoryAccountWithAccount) {
                    selectedItem.let { itemToEdit ->
                        val gson = Gson()
                        val json = gson.toJson(itemToEdit)
                        val intent = Intent(requireContext(), RevenueAndExpenditureActivity::class.java)
                        intent.putExtra("itemToEditAccount", json)
                        startActivity(intent)
                    }
                }
                true
            }

            R.id.action_delete -> {
                conformDelete()
                true
            }

            R.id.action_detail -> {
                val selectedItem = adapter.getSelectedItem()
                if (selectedItem is IncomeExpenseListData) {
                    selectedItem.let { itemToEdit ->
                        val gson = Gson()
                        val json = gson.toJson(itemToEdit)
                        val intent = Intent(requireContext(), DetailActivity::class.java)
                        intent.putExtra("itemDetail", json)
                        startActivity(intent)
                    }
                } else if (selectedItem is HistoryAccountWithAccount) {
                    selectedItem.let { itemToEdit ->
                        val gson = Gson()
                        val json = gson.toJson(itemToEdit)
                        val intent = Intent(requireContext(), DetailActivity::class.java)
                        intent.putExtra("accountDetail", json)
                        startActivity(intent)
                    }
                }

                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun conformDelete() {
        context.let { cxt ->
            val dialog = Dialog(cxt!!)
            dialog.setContentView(R.layout.layout_confirm_delete)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val backDeleteBtn = dialog.findViewById<View>(R.id.backDeleteBtn)
            val successDeleteBtn = dialog.findViewById<View>(R.id.successDeleteBtn)

            backDeleteBtn.setOnClickListener {
                dialog.dismiss()
                adapter.notifyItemChanged(selectedItemPosition ?: return@setOnClickListener)
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
                                    requireContext(),
                                    "Đã xóa thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                        }
                    }
                } else if (selectedItem is HistoryAccountWithAccount) {
                    val historyAccount = selectedItem.historyAccount
                    val accountTransfer = selectedItem.accountTransfer
                    val accountReceive = selectedItem.accountReceive

                    val amountAccount1 =
                        accountTransfer.amountAccount.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val amountAccount2 =
                        accountReceive.amountAccount.replace(",", ".").toDoubleOrNull() ?: 0.0

                    val transferAmount =
                        historyAccount.transferAmount.replace(",", ".").toDoubleOrNull() ?: 0.0

                    val totalAmount = transferAmount + amountAccount1
                    val minusAmount = amountAccount2 - transferAmount

                    val accountFormat1 =
                        accountTransfer.copy(amountAccount = totalAmount.toString())
                    val accountFormat2 = accountReceive.copy(amountAccount = minusAmount.toString())

                    historyAccountViewModel.deleteHistoryAccount(historyAccount)
                    accountViewModel.updateListAccounts(listOf(accountFormat1, accountFormat2))
                        .observe(viewLifecycleOwner) { success ->
                            if (success) {
                                Toast.makeText(
                                    requireContext(),
                                    "Đã xóa thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Xóa thất bại",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                        }
                }
            }

            dialog.show()
        }
    }

    override fun onItemClick(incomeExpense: Any) {
        if (incomeExpense is IncomeExpenseListData) {
            incomeExpense.let { itemToEdit ->
                val gson = Gson()
                val json = gson.toJson(itemToEdit)
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("itemDetail", json)
                startActivity(intent)
            }
        } else if (incomeExpense is HistoryAccountWithAccount) {
            incomeExpense.let { itemToEdit ->
                val gson = Gson()
                val json = gson.toJson(itemToEdit)
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("accountDetail", json)
                startActivity(intent)
            }
        }
    }

    private fun setupNightMode() {
        val currentNightMode =
            requireActivity().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                checkMode = false
                binding.searchBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black1
                    )
                )

                binding.calendarBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black1
                    )
                )

                binding.iconCalendar.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black1
                    )
                )

                binding.titleBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow
                    )
                )
                val color = ContextCompat.getColor(requireContext(), R.color.yellow)
                requireActivity().window.statusBarColor = color
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                checkMode = true
                binding.searchBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.calendarBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
        }
    }
}
