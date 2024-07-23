package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.text.DecimalFormat

data class LegendItem(val category: String, val color: Int, val amount: String, var isSelected: Boolean = false)

class LegendAdapter(private val items: List<LegendItem>, private val totalAmount: Float) :
    RecyclerView.Adapter<LegendAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorCircle: View = itemView.findViewById(R.id.color_circle)
        val textCategory: TextView = itemView.findViewById(R.id.text_category)
        val textPercentage: TextView = itemView.findViewById(R.id.text_percentage)
        val itemContainer: View = itemView.findViewById(R.id.item_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_legend_chart, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val background = holder.colorCircle.background as GradientDrawable
        background.setColor(item.color)

        holder.textCategory.text = item.category

        val amountString = item.amount.replace(",", ".")
        val amountFloat = amountString.toFloatOrNull() ?: 0f

        val expenseFormatter = DecimalFormat("#,###.##")

        val percentage = if (totalAmount != 0f) (amountFloat / totalAmount) * 100 else 0f
        val formattedPercentage = expenseFormatter.format(percentage)

        holder.textPercentage.text = "$formattedPercentage%"

        if (item.isSelected) {
            holder.itemContainer.setBackgroundColor(Color.YELLOW)
            holder.textCategory.setTextColor(Color.BLACK)
            holder.textPercentage.setTextColor(Color.BLACK)
        } else {
            holder.itemContainer.setBackgroundColor(Color.TRANSPARENT)
            holder.textCategory.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray1))
            holder.textPercentage.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray1))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
