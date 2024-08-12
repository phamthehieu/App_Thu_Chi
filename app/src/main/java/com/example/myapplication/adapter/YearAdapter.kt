package com.example.myapplication.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import java.time.LocalDate

class YearAdapter(
    context: Context,
    private val years: List<String>,
    private var yearSelected: String
) : ArrayAdapter<String>(context, R.layout.year_item, years) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val currentYear = LocalDate.now().year.toString()

    private class ViewHolder(view: View) {
        val yearTextView: TextView = view.findViewById(R.id.yearTextView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.year_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val year = getItem(position)
        viewHolder.yearTextView.text = year

        if (year == currentYear) {
            viewHolder.yearTextView.setBackgroundResource(R.drawable.round_item_year_defaultt)
        } else if (year == yearSelected) {
            viewHolder.yearTextView.setBackgroundResource(R.drawable.rounded_corner_yellow)
        } else {
            viewHolder.yearTextView.background = null
        }

        return view
    }

    fun setSelectedYear(selectedYear: String) {
        yearSelected = selectedYear
    }
}
