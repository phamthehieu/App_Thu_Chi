package com.example.myapplication.view.account

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AccountTransferAdapter
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.databinding.FragmentBottomSelectAccountTransferBinding
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.SharedAccountSelectedModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSelectAccountTransferFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSelectAccountTransferBinding

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(requireActivity().application)
    }

    companion object {
        private const val ARG_ACCOUNT1 = "arg_account1"
        private const val ARG_ACCOUNT2 = "arg_account2"
        private const val ARG_IS_ACCOUNT_1_SELECTED = "is_account_1_selected"

        fun newInstance(
            account1: AccountIconFormat?,
            account2: AccountIconFormat?,
            isAccount1Selected: Boolean
        ): BottomSelectAccountTransferFragment {
            val fragment = BottomSelectAccountTransferFragment()
            val args = Bundle()
            args.putParcelable(ARG_ACCOUNT1, account1)
            args.putParcelable(ARG_ACCOUNT2, account2)
            args.putBoolean(ARG_IS_ACCOUNT_1_SELECTED, isAccount1Selected)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSelectAccountTransferBinding.inflate(inflater, container, false)

        binding.accountManagerBtn.setOnClickListener {
            val accountManagementIntent = Intent(context, AccountManagementActivity::class.java)
            context?.startActivity(accountManagementIntent)
        }

        binding.backBtn.setOnClickListener {
            dismiss()
        }

        binding.addNewAccountBtn.setOnClickListener {
            val addNewAccountIntent = Intent(context, AddNewAccountActivity::class.java)
            context?.startActivity(addNewAccountIntent)
        }

        setupRecyclerView()

        setupNightMode()

        return binding.root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerViewAccountTransfer
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val account1 = arguments?.getParcelable<AccountIconFormat>(ARG_ACCOUNT1)
        val account2 = arguments?.getParcelable<AccountIconFormat>(ARG_ACCOUNT2)

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

            if (formattedAccounts.isEmpty()) {
                binding.recyclerViewAccountTransfer.visibility = View.GONE
                binding.noData.visibility = View.VISIBLE
            } else {
                binding.recyclerViewAccountTransfer.visibility = View.VISIBLE
                binding.noData.visibility = View.GONE
            }

            val adapter = AccountTransferAdapter(formattedAccounts, account1, account2) { account ->
                val result = Bundle().apply {
                    putParcelable("selected_account", account)
                }
                parentFragmentManager.setFragmentResult("requestKey", result)
                dismiss()
            }

            recyclerView.adapter = adapter
        }
    }


    private fun setupNightMode() {
        val currentNightMode =
            requireActivity().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.accountManagerBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black1
                    )
                )

                binding.backBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black1
                    )
                )

            }

            Configuration.UI_MODE_NIGHT_YES -> {
                binding.accountManagerBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                binding.backBtn.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
        }
    }
}