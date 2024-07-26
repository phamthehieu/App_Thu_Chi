package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.interfaces.OnMonthSelectedListener

class MonthPagerAdapter(
    private val context: Context,
    private val data: List<Int>,
    private val check: Boolean,
    private var yearData: Int,
    var selectedMonth: Int,
    private val onMonthSelectedListener: OnMonthSelectedListener
) : RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateYearData(newYearData: Int) {
        this.yearData = newYearData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.viewpager_item_month, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bind(position, selectedMonth)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)

        init {
            recyclerView.layoutManager = GridLayoutManager(context, 4)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun bind(position: Int, selectedMonth: Int) {
            val adapter = MonthAdapter(context, data, check, selectedMonth) { newSelectedMonth ->
                val previousSelectedMonth = this@MonthPagerAdapter.selectedMonth
                this@MonthPagerAdapter.selectedMonth = newSelectedMonth
                onMonthSelectedListener.onMonthSelected(newSelectedMonth)
                // Notify the adapter for the specific ViewHolder
                recyclerView.adapter?.notifyItemChanged(data.indexOf(previousSelectedMonth))
                recyclerView.adapter?.notifyItemChanged(data.indexOf(newSelectedMonth))
            }
            recyclerView.adapter = adapter
        }
    }
}



