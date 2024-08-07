package com.example.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.IncomeExpenseListAdapter.Totals
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.utilities.AccountTypeProvider.accountTypes
import com.example.myapplication.view.account.AddNewAccountActivity
import com.example.myapplication.view.account.BottomSheetTypeAccountFragment
import java.text.DecimalFormat
import java.time.format.DateTimeParseException

class ListAccountAdapter(
    private val accountList: Map<String, List<AccountIconFormat>>,
    private val context: Context,
    private val itemClickListenerAccount: OnItemClickListenerAccount
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListenerAccount {
        fun onItemClick(dataAccount: Any)
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
        const val TYPE_FOOTER = 2
    }

    private val dataList: MutableList<Any> = mutableListOf<Any>().apply {
        accountList.forEach { (type, accountList) ->
            if (type != "0") {
                add(type)
            }
            addAll(accountList)
        }
        add("FOOTER")
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dataList[position] is String && position == dataList.size - 1 -> TYPE_FOOTER
            dataList[position] is String -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_layout_header_account, parent, false)
                HeaderViewHolder(view)
            }

            TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_layout_footer_account, parent, false)
                FooterViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_layout_account, parent, false)
                AccountViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val headerTitle = dataList[position] as String
                holder.bind(headerTitle)
            }

            is AccountViewHolder -> {
                val account = dataList[position] as AccountIconFormat
                holder.bind(account)
            }

            is FooterViewHolder -> {
                holder.addNewAccountBtn.setOnClickListener {
                    val addNewAccountIntent = Intent(context, AddNewAccountActivity::class.java)
                    context.startActivity(addNewAccountIntent)
                }
            }
        }

        holder.itemView.setOnClickListener {
            when (holder) {
                is AccountViewHolder -> {
                    itemClickListenerAccount.onItemClick(dataList[position])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun calculateTotalsByType(): Map<Int, Double> {
        val totalsByType = mutableMapOf<Int, Double>()

        for ((type, accounts) in accountList) {
            var totalAmount = 0.0

            for (account in accounts) {
                val amount = account.amountAccount.replace(",", ".").toDoubleOrNull() ?: 0.0
                totalAmount += amount
            }

            totalsByType[type.toInt()] = totalAmount
        }

        return totalsByType
    }


    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeAccount: TextView = itemView.findViewById(R.id.typeAccount)
        private val totalAmountAccount: TextView = itemView.findViewById(R.id.totalAmountAccount)
        private val titleTotalAmount: TextView = itemView.findViewById(R.id.titleTotalAmount)
        fun bind(title: String) {
            val typeAccountId = title.toInt()
            val data = accountTypes[typeAccountId]
            typeAccount.text = data.name
            titleTotalAmount.visibility =
                if (typeAccountId == 3 || typeAccountId == 7) View.VISIBLE else View.GONE
            val totals = calculateTotalsByType()
            val totalAmount = totals[typeAccountId] ?: 0

            val expenseFormatter = DecimalFormat("#,###.##")

            totalAmountAccount.text = expenseFormatter.format(totalAmount)
        }
    }

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameAccountTV)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountAccountTV)
        private val iconImageView: ImageView = itemView.findViewById(R.id.imageAccountIV)
        private val noteTextView: TextView = itemView.findViewById(R.id.noteAccountTV)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleAccountTV)

        fun bind(account: AccountIconFormat) {
            nameTextView.text = account.nameAccount
            noteTextView.text = account.note
            noteTextView.visibility = if (account.note.isEmpty()) View.GONE else View.VISIBLE

            val amount = account.amountAccount.replace(",", ".").toDouble()
            val expenseFormatter = DecimalFormat("#,###.##")
            amountTextView.text = expenseFormatter.format(amount)

            iconImageView.setImageResource(account.iconResource)
            iconImageView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black))

            val typeAccountId = account.typeAccount
            titleTextView.visibility =
                if (typeAccountId == 3 || typeAccountId == 7) View.VISIBLE else View.GONE
        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addNewAccountBtn: TextView = itemView.findViewById(R.id.addNewAccountBtn)
        val successBtn: TextView = itemView.findViewById(R.id.successBtn)
    }
}

