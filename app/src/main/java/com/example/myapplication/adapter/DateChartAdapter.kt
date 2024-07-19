package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.CombinedCategoryIcon

class DateChartAdapter(
    private val dates: MutableList<String>,
    private val onLoadMoreClicked: () -> Unit,
    private val onDateClickListener: OnDateClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOAD_MORE = 1

    interface OnDateClickListener {
        fun onDateClick(date: String)
    }


    var selectedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_chart, parent, false)
            DateChartViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_load_more, parent, false)
            LoadMoreViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return dates.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == dates.size) VIEW_TYPE_LOAD_MORE else VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateChartViewHolder) {
            val date = dates[position]
            holder.dateTextView.text = date

            if (position == selectedPosition) {
                holder.selectionIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            } else {
                holder.selectionIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.black1))
            }

            holder.dateTextView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onDateClickListener.onDateClick(date)
            }
        } else if (holder is LoadMoreViewHolder) {
            holder.loadMoreButton.setOnClickListener {
                onLoadMoreClicked()
            }
        }
    }



    class DateChartViewHolder(dateView: View) : RecyclerView.ViewHolder(dateView) {
        val dateTextView: TextView = dateView.findViewById(R.id.dateTextView)
        val selectionIndicator: View = dateView.findViewById(R.id.selectionIndicator)
    }

    class LoadMoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val loadMoreButton: ImageButton = view.findViewById(R.id.loadMoreButton)
    }
}
