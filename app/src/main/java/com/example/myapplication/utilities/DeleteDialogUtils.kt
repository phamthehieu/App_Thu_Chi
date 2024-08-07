package com.example.myapplication.utilities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.example.myapplication.R
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.entity.Account
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DeleteDialogUtils {


    @OptIn(DelicateCoroutinesApi::class)
    fun showDeleteDialog(
        context: Context,
        account: AccountIconFormat,
        accountViewModel: AccountViewModel,
        onSuccess: () -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.layout_confirm_delete)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backDeleteBtn = dialog.findViewById<View>(R.id.backDeleteBtn)
        val successDeleteBtn = dialog.findViewById<View>(R.id.successDeleteBtn)

        backDeleteBtn.setOnClickListener {
            dialog.dismiss()
        }

        successDeleteBtn.setOnClickListener {
            val dataDelete = Account(
                id = account.id,
                nameAccount = account.nameAccount,
                typeAccount = account.typeAccount,
                amountAccount = account.amountAccount,
                icon = account.icon,
                note = account.note
            )
            accountViewModel.deleteAccount(dataDelete)
            Toast.makeText(context, "Đã xóa thành công", Toast.LENGTH_SHORT).show()
            onSuccess()
            dialog.dismiss()
        }

        dialog.show()
    }
}