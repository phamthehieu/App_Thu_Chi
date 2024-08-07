package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.utilities.ItemMoveCallback
import java.util.Collections

class AccountManagerAdapter(
    private val accounts: List<AccountIconFormat>,
    private val itemClickSettingAccount: OnItemClickSettingAccount,
    private val pinnedAccountIds: Set<Int>
) : RecyclerView.Adapter<AccountManagerAdapter.AccountViewHolder>(), ItemMoveCallback {

    private var itemTouchHelper: ItemTouchHelper? = null

    interface OnItemClickSettingAccount {
        fun onDeleteClick(account: AccountIconFormat)
        fun onEditClick(account: AccountIconFormat)
        fun onPinClick(account: AccountIconFormat)
        fun onOrderChanged(newOrder: List<AccountIconFormat>)
    }

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iconAccountManager)
        val nameAccount: TextView = view.findViewById(R.id.nameAccountManagerTV)
        val deleteButton: ImageView = view.findViewById(R.id.deleteAccountManager)
        val editButton: ImageView = view.findViewById(R.id.editAccountManager)
        val pinButton: ImageView = view.findViewById(R.id.pinAccountManager)
        val noteAccount: TextView = view.findViewById(R.id.noteAccountManagerTV)
        val dragHandle: ImageView = view.findViewById(R.id.dragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_account_managerment, parent, false)
        return AccountViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        holder.icon.setImageResource(account.iconResource)
        holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))

        holder.nameAccount.text = account.nameAccount
        holder.noteAccount.text = account.note
        holder.noteAccount.visibility = if (account.note.isEmpty()) View.GONE else View.VISIBLE

        holder.deleteButton.setOnClickListener {
            itemClickSettingAccount.onDeleteClick(account)
        }
        holder.editButton.setOnClickListener {
            itemClickSettingAccount.onEditClick(account)
        }

        if (pinnedAccountIds.contains(account.id)) {
            holder.pinButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
        } else {
            holder.pinButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.gray))
        }

        holder.pinButton.setOnClickListener {
            itemClickSettingAccount.onPinClick(account)
        }

        holder.dragHandle.setOnTouchListener { _, _ ->
            itemTouchHelper?.startDrag(holder)
            true
        }
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val fromAccount = accounts[fromPosition]
        val toAccount = accounts[toPosition]

        if (pinnedAccountIds.contains(fromAccount.id) || pinnedAccountIds.contains(toAccount.id)) {
            return false
        }

        Collections.swap(accounts, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)

        itemClickSettingAccount.onOrderChanged(accounts)

        return true
    }

    fun setItemTouchHelper(helper: ItemTouchHelper) {
        this.itemTouchHelper = helper
    }
}
