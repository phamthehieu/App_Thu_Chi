package com.example.myapplication.view.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.ListAccountAdapter
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.databinding.FragmentAccountBinding
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.google.gson.Gson
import java.text.DecimalFormat

class AccountFragment : Fragment(), ListAccountAdapter.OnItemClickListenerAccount {

    private lateinit var binding: FragmentAccountBinding

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        renderListAccount()

        binding.imageTitleAccount.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )

        return binding.root
    }

    private fun renderListAccount() {
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
            }

            val (liabilitiesMustPayTotal, assetTotal) = calculateTotals(formattedAccounts)

            val netValue = assetTotal - liabilitiesMustPayTotal

            val expenseFormatter = DecimalFormat("#,###.##")

            binding.netValue.text = expenseFormatter.format(netValue)
            binding.asset.text = expenseFormatter.format(assetTotal)
            binding.liabilitiesMustPay.text = expenseFormatter.format(liabilitiesMustPayTotal)

            val sortedAccounts = formattedAccounts.sortedBy { it.typeAccount }
            val accountFormat = groupIconsByType(sortedAccounts)
            val adapter = ListAccountAdapter(accountFormat, requireContext(), this)
            recyclerView.adapter = adapter
        }
    }

    private fun groupIconsByType(data: List<AccountIconFormat>): Map<String, List<AccountIconFormat>> {
        return data.groupBy { it.typeAccount.toString() }
    }

    private fun calculateTotals(accounts: List<AccountIconFormat>): Pair<Double, Double> {

        fun parseAmount(amount: String): Double {
            return try {
                amount.replace(',', '.').toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        }

        val type3And7Total = accounts
            .filter { it.typeAccount == 3 || it.typeAccount == 7 }
            .sumOf { parseAmount(it.amountAccount) }

        val otherTypesTotal = accounts
            .filter { it.typeAccount != 3 && it.typeAccount != 7 }
            .sumOf { parseAmount(it.amountAccount) }

        return Pair(type3And7Total, otherTypesTotal)
    }

    override fun onItemClick(dataAccount: Any) {
        val gson = Gson()
        val json = gson.toJson(dataAccount)
        val intent = Intent(requireContext(), AccountDetailsActivity::class.java)
        intent.putExtra("dataAccount", json)
        startActivity(intent)
    }


}