package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AccountType

class AccountTypeAdapter(private val accountTypes: List<AccountType>, private val onItemClick: (AccountType, Int) -> Unit) :
    RecyclerView.Adapter<AccountTypeAdapter.AccountTypeViewHolder>() {

    inner class AccountTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_type_new_account, parent, false)
        return AccountTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountTypeViewHolder, position: Int) {
        holder.textView.text = accountTypes[position].name
        holder.textViewTitle.text = accountTypes[position].type
        holder.itemView.setOnClickListener {
            onItemClick(accountTypes[position], position)
        }
    }

    override fun getItemCount(): Int {
        return accountTypes.size
    }
}
