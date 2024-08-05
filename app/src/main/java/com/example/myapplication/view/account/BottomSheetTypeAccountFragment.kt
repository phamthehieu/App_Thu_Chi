package com.example.myapplication.view.account

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.AccountTypeAdapter
import com.example.myapplication.data.AccountType
import com.example.myapplication.databinding.FragmentBottomSheetTypeAccpuntBinding
import com.example.myapplication.utilities.AccountTypeProvider.accountTypes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetTypeAccountFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetTypeAccpuntBinding

    interface OnAccountTypeSelectedListener {
        fun onAccountTypeSelected(accountType: AccountType, position: Int)
    }

    private var listener: OnAccountTypeSelectedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(resources.getColor(R.color.black1, null))

            bottomSheet?.viewTreeObserver?.addOnGlobalLayoutListener {
                val rect = android.graphics.Rect()
                bottomSheet.getWindowVisibleDisplayFrame(rect)
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetTypeAccpuntBinding.inflate(inflater, container, false)

        val recyclerView: RecyclerView = binding.accountTypeRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = AccountTypeAdapter(accountTypes) { selectedAccountType, position ->
            listener?.onAccountTypeSelected(selectedAccountType, position)
            dismiss()
        }

        return binding.root
    }

    fun setOnAccountTypeSelectedListener(listener: AddNewAccountActivity) {
        this.listener = listener
    }
}
