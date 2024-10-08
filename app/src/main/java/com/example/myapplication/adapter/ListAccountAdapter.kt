package com.example.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.utilities.AccountTypeProvider.accountTypes
import com.example.myapplication.view.account.AccountManagementActivity
import com.example.myapplication.view.account.AddNewAccountActivity
import java.text.DecimalFormat

class ListAccountAdapter(
    private val accountList: Map<String, List<AccountIconFormat>>,
    private val context: Context,
    private val itemClickListenerAccount: OnItemClickListenerAccount
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListenerAccount {
        fun onItemClick(dataAccount: Any)
        fun onDeleteClick(account: Any)
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
                holder.accountManagementBtn.setOnClickListener {
                    val accountManagementIntent = Intent(context, AccountManagementActivity::class.java)
                    context.startActivity(accountManagementIntent)
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

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameAccountTV)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountAccountTV)
        private val iconImageView: ImageView = itemView.findViewById(R.id.imageAccountIV)
        private val noteTextView: TextView = itemView.findViewById(R.id.noteAccountTV)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleAccountTV)
        private lateinit var currentAccount: AccountIconFormat

        init {
            itemView.setOnLongClickListener(this)
        }

        fun bind(account: AccountIconFormat) {
            currentAccount = account

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

        override fun onLongClick(v: View?): Boolean {
            showPopupMenu(v)
            return true
        }

        private fun showPopupMenu(view: View?) {
            val popup = PopupMenu(view?.context, view)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.account_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.account_edit -> {
                        handleEditAccount(currentAccount)
                        true
                    }
                    R.id.account_delete -> {
                        handleDeleteAccount(currentAccount)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun handleEditAccount(account: AccountIconFormat) {
            val intent = Intent(itemView.context, AddNewAccountActivity::class.java).apply {
                putExtra("type", "edit")
                putExtra("Account", account)
            }
            itemView.context.startActivity(intent)
        }

        private fun handleDeleteAccount(account: AccountIconFormat) {
            itemClickListenerAccount.onDeleteClick(account)
        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addNewAccountBtn: TextView = itemView.findViewById(R.id.addNewAccountBtn)
        val accountManagementBtn: TextView = itemView.findViewById(R.id.accountManagementBtn)
    }
}
