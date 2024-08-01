package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.MonthlyReport
import java.text.DecimalFormat

class MonthlyReportAdapter(private val reports: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val yearTv: TextView = view.findViewById(R.id.yearTv)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthTv: TextView = view.findViewById(R.id.monthTv)
        val expenseTv: TextView = view.findViewById(R.id.allExpenseTv)
        val incomeTv: TextView = view.findViewById(R.id.allIncomeTv)
        val balanceTv: TextView = view.findViewById(R.id.balanceTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_year_header_detailed_starts, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_detailed_starts, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val year = reports[position] as String
            holder.yearTv.text = year
        } else if (holder is ItemViewHolder) {
            val decimalFormat = DecimalFormat("#,###.##")
            val report = reports[position] as MonthlyReport
            holder.monthTv.text = report.month
            holder.expenseTv.text = decimalFormat.format(report.expense)
            holder.incomeTv.text = decimalFormat.format(report.income)
            holder.balanceTv.text = decimalFormat.format(report.balance)
        }
    }

    override fun getItemCount() = reports.size

    override fun getItemViewType(position: Int): Int {
        return if (reports[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }
}