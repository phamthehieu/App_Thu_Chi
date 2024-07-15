package com.example.myapplication.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.entity.IncomeExpenseList

class IncomeExpenseListAdapter(private val groupedIconMap: Map<String, List<IncomeExpenseList>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val dataList: List<Any> = mutableListOf<Any>().apply {
        groupedIconMap.forEach { (type, incomeExpenseList) ->
            add(type)
            addAll(incomeExpenseList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}