package com.example.myapplication.view.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AccountSelectedAdapter
import com.example.myapplication.adapter.ListAccountAdapter
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.databinding.FragmentBottomSelectedAccountBinding
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.SharedAccountSelectedModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.DecimalFormat

class BottomSelectedAccountFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSelectedAccountBinding

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(requireActivity().application)
    }

    private val sharedViewModel: SharedAccountSelectedModel by activityViewModels()

    private var selectedAccount: AccountIconFormat? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSelectedAccountBinding.inflate(inflater, container, false)

        binding.accountManagerBtn.setOnClickListener {
            val accountManagementIntent = Intent(context, AccountManagementActivity::class.java)
            context?.startActivity(accountManagementIntent)
        }

        setupRecyclerView()

        setupSwitch()

        return binding.root
    }

    private fun setupSwitch() {
        sharedViewModel.selectedAccount.observe(viewLifecycleOwner) { account ->
            selectedAccount = account
        }

        binding.switchAccount.setOnCheckedChangeListener { _, isChecked ->
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("switchState", isChecked)
            editor.apply()
        }

        binding.backBtn.setOnClickListener {
            dismiss()
        }

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val switchState = sharedPreferences.getBoolean("switchState", false)

        binding.switchAccount.isChecked = switchState
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerViewAccount
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        accountTypeViewModel.allAccounts.observe(viewLifecycleOwner) { accounts ->
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

            val setting = AccountIconFormat(
                id = -1,
                nameAccount = "Không liên kết bất kì tài khoản nào",
                typeAccount = -1,
                amountAccount = "0",
                icon = -1,
                note = "",
                iconResource = R.drawable.ic_credit_card_50,
                typeIcon = "Account"
            )

            formattedAccounts.add(setting)

            val adapter = AccountSelectedAdapter(formattedAccounts) { account ->
                sharedViewModel.selectAccount(account)
                dismiss()
            }

            recyclerView.adapter = adapter

            sharedViewModel.selectedAccount.observe(viewLifecycleOwner) { selectedAccount ->
                adapter.setSelectedAccount(selectedAccount)
            }
        }
    }
}
