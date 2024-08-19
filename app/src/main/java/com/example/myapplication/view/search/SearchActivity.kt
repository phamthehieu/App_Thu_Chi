package com.example.myapplication.view.search

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.CategoriesSearchAdapter
import com.example.myapplication.adapter.IncomeExpenseListAdapter
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivitySearchBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.interfaces.OnMonthSelectedListener
import com.example.myapplication.utilities.CategoryRepository
import com.example.myapplication.view.home.DetailActivity
import com.example.myapplication.view.home.SwipeGesture
import com.example.myapplication.view.revenue_and_expenditure.RevenueAndExpenditureActivity
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

class SearchActivity : AppCompatActivity(), CategoriesSearchAdapter.OnItemClickListenerSearch, OnMonthSelectedListener,  IncomeExpenseListAdapter.OnItemClickListener {

    private lateinit var binding: ActivitySearchBinding

    private lateinit var adapter: CategoriesSearchAdapter

    private lateinit var adapter2: IncomeExpenseListAdapter

    private var valueSearch: String = ""
    private var selectedItemPosition: Int? = null

    private val dataListStyle = mutableListOf<String>()
    private var listCategory: List<Pair<Int, String>>? = null

    private val combinedData = MediatorLiveData<List<Any>>()

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    private val historyAccountViewModel: HistoryAccountViewModel by viewModels {
        HistoryAccountViewModelFactory(this.application)
    }

    private val accountViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(this.application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNightMode()
        setupBackground()
        setupRecyclerView()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.searchBtn.setOnClickListener {
            searchData()
            hideKeyboard()
        }
    }

    private fun searchData() {
        val categoryIdList: List<Int> = listCategory?.map { it.first } ?: emptyList()

        val transferData = historyAccountViewModel.getHistoryAccountWithSearch(valueSearch)
        val expenseIncomeData = incomeExpenseListModel.getListIncomeExpenseWithSearch("", valueSearch, categoryIdList)
        val expenseData = incomeExpenseListModel.getListIncomeExpenseWithSearch("Expense", valueSearch, categoryIdList)
        val incomeData = incomeExpenseListModel.getListIncomeExpenseWithSearch("Income", valueSearch, categoryIdList)
        if (dataListStyle.isNotEmpty()) {
            if (dataListStyle.contains("Transfer")) {
                combinedData.addSource(transferData) { data ->
                    if (!data.isNullOrEmpty()) {
                        combinedData.value = combinedData.value.orEmpty() + data
                    }
                }
            }
            if (dataListStyle.containsAll(listOf("Expense", "Income"))) {
                combinedData.addSource(expenseIncomeData) { data ->
                    if (!data.isNullOrEmpty()) {
                        addIncomeExpenseDataToCombinedData(data)
                    }
                }
            } else if (dataListStyle.contains("Expense")) {
                combinedData.addSource(expenseData) { data ->
                    if (!data.isNullOrEmpty()) {
                        addIncomeExpenseDataToCombinedData(data)
                    }
                }
            } else if (dataListStyle.contains("Income")) {
                combinedData.addSource(incomeData) { data ->
                    if (!data.isNullOrEmpty()) {
                        addIncomeExpenseDataToCombinedData(data)
                    }
                }
            }
        } else {
            combinedData.addSource(transferData) { data ->
                if (!data.isNullOrEmpty()) {
                    combinedData.value = combinedData.value.orEmpty() + data
                }
            }
            combinedData.addSource(expenseIncomeData) { data ->
                if (!data.isNullOrEmpty()) {
                    addIncomeExpenseDataToCombinedData(data)
                }
            }
        }
    }

    private fun addIncomeExpenseDataToCombinedData(data: List<CategoryWithIncomeExpenseList>?) {
        val convertedData = data?.map { convertToIncomeExpenseListData(it) } ?: emptyList()
        if (convertedData.isNotEmpty()) {
            combinedData.value = combinedData.value.orEmpty() + convertedData
        }
    }

    private fun convertToIncomeExpenseListData(item: CategoryWithIncomeExpenseList): IncomeExpenseListData {
        return IncomeExpenseListData(
            id = item.incomeExpense.id,
            note = item.incomeExpense.note,
            amount = item.incomeExpense.amount,
            date = item.incomeExpense.date,
            categoryId = item.incomeExpense.categoryId,
            type = item.incomeExpense.type,
            image = item.incomeExpense.image,
            categoryName = item.category.name,
            iconResource = item.incomeExpense.iconResource,
            idIcon = item.category.id,
            accountId = item.incomeExpense.accountId,
        )
    }

    override fun onResume() {
        super.onResume()
        updateRecyclerView()
        updateSearchBtnState()
    }

    private fun setupRecyclerView() {
        val categories = CategoryRepository.getCategories()
        listCategory = categories
        adapter = CategoriesSearchAdapter(categories, this)

        binding.recyclerViewSearch.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewSearch.adapter = adapter
    }

    private fun updateRecyclerView() {
        val categories = CategoryRepository.getCategories()
        listCategory = categories
        adapter.updateData(categories)
        updateSearchBtnState()
    }

    private fun setupBackground() {
        binding.textViewNumberDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                valueSearch = s.toString()
                if (valueSearch.isNotEmpty()) {
                    binding.iconEnd.visibility = View.VISIBLE
                } else {
                    binding.iconEnd.visibility = View.GONE
                }
                updateSearchBtnState()
            }
        })

        binding.iconEnd.setOnClickListener {
            binding.textViewNumberDisplay.text.clear()
        }

        binding.allStyle.setOnClickListener {
            dataListStyle.clear()
            updateUI()
            updateSearchBtnState()
        }

        binding.expense.setOnClickListener {
            val data = "Expense"
            removeFromDataList(data)
            updateUI()
            updateSearchBtnState()
        }

        binding.income.setOnClickListener {
            val data = "Income"
            removeFromDataList(data)
            updateUI()
            updateSearchBtnState()
        }

        binding.transfer.setOnClickListener {
            val data = "Transfer"
            removeFromDataList(data)
            updateUI()
            updateSearchBtnState()
        }

        binding.resetBtn.setOnClickListener {
            CategoryRepository.clearCategories()
            dataListStyle.clear()
            binding.textViewNumberDisplay.text.clear()
            combinedData.value = emptyList()
            binding.iconEnd.visibility = View.GONE
            valueSearch = ""
            updateUI()
            updateRecyclerView()
            updateSearchBtnState()
        }

        binding.recyclerViewSearchData.layoutManager = LinearLayoutManager(this)

        combinedData.observe(this) { combinedList ->
            val incomeExpenseList = combinedList.filterIsInstance<IncomeExpenseListData>()
            val historyAccountList = combinedList.filterIsInstance<HistoryAccountWithAccount>()
            val combinedForAdapter = groupIconsByType((incomeExpenseList + historyAccountList))
            adapter2 = IncomeExpenseListAdapter(combinedForAdapter, this)
            binding.recyclerViewSearchData.adapter = adapter2

            val swipeGesture = object : SwipeGesture(this) {
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
                        adapter2.notifyItemChanged(viewHolder.adapterPosition)
                        return
                    }

                    val position = viewHolder.adapterPosition
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            selectedItemPosition = position
                            adapter2.removeItem(position)
                            conformDelete()
                        }

                        ItemTouchHelper.RIGHT -> {
                            selectedItemPosition = position
                            adapter2.removeItem(position)
                            val selectedItem = adapter2.getSelectedItem()
                            selectedItem?.let {
                                val gson = Gson()
                                val json = gson.toJson(it)
                                val  intent = Intent(this@SearchActivity, RevenueAndExpenditureActivity::class.java)
                                intent.putExtra("itemToEdit", json)
                                startActivity(intent)
                                adapter2.notifyItemChanged(selectedItemPosition ?: return@let)
                            }
                        }
                    }
                }
            }

            val itemTouchHelper = ItemTouchHelper(swipeGesture)
            itemTouchHelper.attachToRecyclerView(binding.recyclerViewSearchData)
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

    private fun updateUI() {
        if (dataListStyle.contains("Expense")) {
            binding.expense.setBackgroundResource(R.drawable.background_gray)
            binding.expenseText.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.expense.setBackgroundResource(R.drawable.background_yellow)
            binding.expenseText.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        if (dataListStyle.contains("Income")) {
            binding.income.setBackgroundResource(R.drawable.background_gray)
            binding.incomeText.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.income.setBackgroundResource(R.drawable.background_yellow)
            binding.incomeText.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        if (dataListStyle.contains("Transfer")) {
            binding.transfer.setBackgroundResource(R.drawable.background_gray)
            binding.transferText.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.transfer.setBackgroundResource(R.drawable.background_yellow)
            binding.transferText.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun removeFromDataList(data: String) {
        if (dataListStyle.contains(data)) {
            dataListStyle.remove(data)
        } else {
            dataListStyle.add(data)
        }

        if (dataListStyle.contains("Expense") &&
            dataListStyle.contains("Income") &&
            dataListStyle.contains("Transfer")
        ) {
            dataListStyle.clear()
            updateUI()
        }
    }

    private fun setupNightMode() {
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        binding.searchBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
        binding.resetBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
                binding.note.setBackgroundResource(R.drawable.background_search_white)
                binding.iconSearch.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.iconEnd.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.textViewNumberDisplay.setTextColor(
                    ContextCompat.getColor(this, R.color.black)
                )
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                val color = ContextCompat.getColor(this, R.color.gray7)
                this.window.statusBarColor = color
                binding.note.setBackgroundResource(R.drawable.background_search_black)
                binding.iconSearch.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.iconEnd.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.textViewNumberDisplay.setTextColor(
                    ContextCompat.getColor(this, R.color.white)
                )
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.gray7))
            }
        }
    }

    private fun handleDeleteClick(category: Pair<Int, String>) {
        CategoryRepository.removeCategoryById(category.first)
        adapter.updateData(CategoryRepository.getCategories())
        updateSearchBtnState()
    }

    override fun onDeleteItem(category: Pair<Int, String>) {
        handleDeleteClick(category)
    }

    override fun onAddButtonClick() {
        startActivity(Intent(this, RevenueAndExpenditureSearchActivity::class.java))
    }

    override fun onAllCategoryClick() {
        CategoryRepository.clearCategories()
        updateRecyclerView()
        updateSearchBtnState()
    }

    private fun updateSearchBtnState() {
        val isSearchBtnEnabled = valueSearch.isNotEmpty() || dataListStyle.isNotEmpty()
        binding.searchBtn.isEnabled = isSearchBtnEnabled
    }

    override fun onMonthSelected(month: Int) {
        TODO("Not yet implemented")
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
                adapter2.notifyItemChanged(selectedItemPosition ?: return@setOnClickListener)
            }

            successDeleteBtn.setOnClickListener {
                val selectedItem = adapter2.getSelectedItem()
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
                                    this@SearchActivity,
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
                        .observe(this) { success ->
                            if (success) {
                                Toast.makeText(
                                   this@SearchActivity,
                                    "Đã xóa thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(
                                    this@SearchActivity,
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

    override fun onItemClick(incomeExpense: Any) {
        if (incomeExpense is IncomeExpenseListData) {
            incomeExpense.let { itemToEdit ->
                val gson = Gson()
                val json = gson.toJson(itemToEdit)
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("itemDetail", json)
                startActivity(intent)
            }
        } else if (incomeExpense is HistoryAccountWithAccount) {
            incomeExpense.let { itemToEdit ->
                val gson = Gson()
                val json = gson.toJson(itemToEdit)
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("accountDetail", json)
                startActivity(intent)
            }
        }
    }

    private fun hideKeyboard() {
        val imm =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.textViewNumberDisplay.windowToken, 0)
    }
}
