package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AccountIconFormat
import java.text.DecimalFormat

class AccountTransferAdapter (
    private val accounts: List<AccountIconFormat>,
    private val account1: AccountIconFormat?,
    private val account2: AccountIconFormat?,
    private val onItemClick: (AccountIconFormat) -> Unit
) : RecyclerView.Adapter<AccountTransferAdapter.AccountViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageAccountIV: ImageView = itemView.findViewById(R.id.imageAccountIV)
        private val nameAccountTV: TextView = itemView.findViewById(R.id.nameAccountTV)
        private val noteAccountTV: TextView = itemView.findViewById(R.id.noteAccountTV)
        private val amountAccountTV: TextView = itemView.findViewById(R.id.amountAccountTV)
        private val titleAccountTV: TextView = itemView.findViewById(R.id.titleAccountTV)
        private val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        private val selectedItemIV: TextView = itemView.findViewById(R.id.selectedItemIV)
        private val container: LinearLayout = itemView.findViewById(R.id.container)

        fun bind(account: AccountIconFormat, position: Int) {
            val amount = account.amountAccount.replace(",", ".").toDouble()
            val expenseFormatter = DecimalFormat("#,###.##")

            linearLayout.visibility = if (account.id == -1) View.GONE else View.VISIBLE

            imageAccountIV.setImageResource(account.iconResource)
            imageAccountIV.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black))

            nameAccountTV.text = account.nameAccount
            noteAccountTV.text = account.note
            amountAccountTV.text = expenseFormatter.format(amount)

            val typeAccountId = account.typeAccount
            titleAccountTV.visibility =
                if (typeAccountId == 3 || typeAccountId == 7) View.VISIBLE else View.GONE

            noteAccountTV.visibility = if (account.note.isNotEmpty()) View.VISIBLE else View.GONE

            if (account.id == -1) {
                container.setBackgroundResource(R.drawable.background_gray)
                imageAccountIV.setColorFilter(ContextCompat.getColor(itemView.context, R.color.white))
            }

            if (account == account1 || account == account2) {
                selectedItemIV.visibility = View.VISIBLE
                if (account == account1) {
                    selectedItemIV.text = "tài khoản 1"
                } else {
                    selectedItemIV.text = "tài khoản 2"
                }
            } else {
                selectedItemIV.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)

                onItemClick(account)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_account_transfer, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position], position)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }
}