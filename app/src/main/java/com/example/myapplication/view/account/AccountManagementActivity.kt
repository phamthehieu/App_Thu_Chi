package com.example.myapplication.view.account

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AccountManagerAdapter
import com.example.myapplication.adapter.DragManageAdapter
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.databinding.ActivityAccountManagementBinding
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.utilities.DeleteDialogUtils
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountManagementActivity : AppCompatActivity(),
    AccountManagerAdapter.OnItemClickSettingAccount {

    private lateinit var binding: ActivityAccountManagementBinding

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(this.application)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("account_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addAccountBtn.setOnClickListener {
            val intent = Intent(this, AddNewAccountActivity::class.java)
            startActivity(intent)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerViewAccountSetting
        recyclerView.layoutManager = LinearLayoutManager(this)

        accountTypeViewModel.allAccounts.observe(this) { accounts ->
            val pinnedIds = getPinnedAccountIds()
            val formattedAccounts = accounts.map { accountWithIcon ->
                AccountIconFormat(
                    id = accountWithIcon.account.id,
                    nameAccount = accountWithIcon.account.nameAccount,
                    typeAccount = accountWithIcon.account.typeAccount,
                    amountAccount = accountWithIcon.account.amountAccount,
                    icon = accountWithIcon.account.icon,
                    note = accountWithIcon.account.note,
                    iconResource = accountWithIcon.icon.iconResource,
                    typeIcon = accountWithIcon.icon.type
                )
            }.toMutableList()

            val savedOrder = loadOrder()

            val orderMap = savedOrder.mapIndexed { index, id -> id to index }.toMap()

            val orderedAccounts =
                formattedAccounts.sortedWith(compareBy { orderMap[it.id] ?: Int.MAX_VALUE })

            val pinnedAccounts = orderedAccounts.filter { pinnedIds.contains(it.id) }

            val unpinnedAccounts = orderedAccounts.filter { !pinnedIds.contains(it.id) }

            val pinnedFirstAccounts = pinnedAccounts + unpinnedAccounts

            val adapter = AccountManagerAdapter(pinnedFirstAccounts, this, pinnedIds)
            recyclerView.adapter = adapter

            val callback = DragManageAdapter(adapter)
            val itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(recyclerView)

            adapter.setItemTouchHelper(itemTouchHelper)
        }
    }


    override fun onDeleteClick(account: AccountIconFormat) {
        DeleteDialogUtils.showDeleteDialog(
            this,
            account,
            accountTypeViewModel
        ) {}
    }

    override fun onEditClick(account: AccountIconFormat) {
        val intent = Intent(this, AddNewAccountActivity::class.java).apply {
            putExtra("type", "edit")
            putExtra("Account", account)
        }
        startActivity(intent)
    }

    override fun onPinClick(account: AccountIconFormat) {
        val pinnedIds = getPinnedAccountIds().toMutableSet()

        if (pinnedIds.contains(account.id)) {
            pinnedIds.remove(account.id)
        } else {
            pinnedIds.clear()
            pinnedIds.add(account.id)
        }

        savePinnedAccountIds(pinnedIds)
        setupRecyclerView()
    }

    override fun onOrderChanged(newOrder: List<AccountIconFormat>) {
        saveOrder(newOrder.map { it.id })
    }

    private fun getPinnedAccountIds(): Set<Int> {
        return sharedPreferences.getStringSet("pinned_accounts", emptySet())?.map { it.toInt() }
            ?.toSet() ?: emptySet()
    }

    private fun savePinnedAccountIds(pinnedIds: Set<Int>) {
        val editor = sharedPreferences.edit()
        editor.putStringSet("pinned_accounts", pinnedIds.map { it.toString() }.toSet())
        editor.apply()
    }

    private fun saveOrder(order: List<Int>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(order)
        editor.putString("account_order", json)
        editor.apply()
    }

    private fun loadOrder(): List<Int> {
        val json = sharedPreferences.getString("account_order", null)
        return if (json != null) {
            val type = object : TypeToken<List<Int>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
