package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.util.Calendar

class MonthAdapter(
    private val context: Context,
    private val data: List<Int>,
    private val check: Boolean,
    private var selectedMonth: Int,
    private val onMonthSelected: (Int) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthTextView: TextView = view.findViewById(R.id.monthTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month, parent, false)
        return MonthViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        if (check) {
            holder.monthTextView.text = data[position].toString()
        } else {
            holder.monthTextView.text = "Thg ${data[position]}"
        }

        // Cập nhật background cho phần tử được chọn
        if (data[position] == selectedMonth) {
            holder.monthTextView.setBackgroundResource(R.drawable.rounded_corner_yellow)
        } else {
            holder.monthTextView.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            val previousSelectedMonth = selectedMonth
            selectedMonth = data[position]
            onMonthSelected(data[position])
            // Cập nhật phần tử trước đó và phần tử mới được chọn
            notifyItemChanged(data.indexOf(previousSelectedMonth))
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}






