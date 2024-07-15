package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entity.IncomeExpenseList
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class IncomeExpenseListAdapter(private val groupedIconMap: Map<String, List<IncomeExpenseList>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    data class Totals(var totalIncome: Double = 0.0, var totalExpense: Double = 0.0)

    private val totalsMap = mutableMapOf<String, Totals>()

    init {
        calculateTotals()
    }

    private fun calculateTotals() {
        for (item in dataList) {
            if (item is IncomeExpenseList) {
                val date = item.date
                val amount = parseAmount(item.amount)
                val totals = totalsMap.getOrPut(date) { Totals() }

                if (item.type == "Income") {
                    totals.totalIncome += amount
                } else if (item.type == "Expense") {
                    totals.totalExpense += amount
                }
            }
        }
    }

    private fun parseAmount(amountStr: String): Double {
        val cleanedAmountStr = amountStr.replace(".", "").replace(",", ".")
        return try {
            cleanedAmountStr.toDouble()
        } catch (e: NumberFormatException) {
            Log.e("IncomeExpenseListAdapter", "Error parsing amount: $amountStr", e)
            0.0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_row_item_header_category_home, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_row_item_category_home, parent, false)
            IncomeExpenseViewHolder(view)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTV: TextView = itemView.findViewById(R.id.dateTV)
        private val weekdaysTV: TextView = itemView.findViewById(R.id.weekdaysTV)
        private val expenseTV: TextView = itemView.findViewById(R.id.expenseTV)
        private val incomeTV: TextView = itemView.findViewById(R.id.incomeTV)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(title: String, totals: Totals) {
            try {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(title, dateFormatter)

                val dayOfWeek = date.dayOfWeek

                val dayOfWeekString = when (dayOfWeek) {
                    DayOfWeek.MONDAY -> "Thứ Hai"
                    DayOfWeek.TUESDAY -> "Thứ Ba"
                    DayOfWeek.WEDNESDAY -> "Thứ Tư"
                    DayOfWeek.THURSDAY -> "Thứ Năm"
                    DayOfWeek.FRIDAY -> "Thứ Sáu"
                    DayOfWeek.SATURDAY -> "Thứ Bảy"
                    DayOfWeek.SUNDAY -> "Chủ Nhật"
                    else -> "Không xác định"
                }

                val newDateFormatter = DateTimeFormatter.ofPattern("dd 'thg' MM")
                val formattedDate = date.format(newDateFormatter)

                val expenseFormatter = DecimalFormat("#,###.##")
                val formattedExpense = expenseFormatter.format(totals.totalExpense)
                val formattedIncome = expenseFormatter.format(totals.totalIncome)

                dateTV.text = formattedDate
                weekdaysTV.text = dayOfWeekString
                expenseTV.text = formattedExpense
                incomeTV.text = formattedIncome

            } catch (e: DateTimeParseException) {
                Log.e("Hieu62", "Invalid date format: $title", e)
            }
        }
    }

    class IncomeExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconCategoryIV: ImageView = itemView.findViewById(R.id.iconCategoryIV)
        private val nameCategoryTV: TextView = itemView.findViewById(R.id.nameCategoryTV)
        private val amountCategoryTV: TextView = itemView.findViewById(R.id.amountCategoryTV)

        @SuppressLint("SetTextI18n")
        fun bind(icon: IncomeExpenseList) {
            iconCategoryIV.setImageResource(icon.iconResource)
            iconCategoryIV.setColorFilter(Color.BLACK)

            if (icon.note.isEmpty()) {
                nameCategoryTV.text = icon.categoryName
            } else {
                nameCategoryTV.text = icon.note
            }

            val amount = icon.amount.replace(",", ".")
                .toDouble()
            val expenseFormatter = DecimalFormat("#,###.##")
            val formattedAmount = expenseFormatter.format(amount)
            if (icon.type == "Expense") {
                amountCategoryTV.text = "-$formattedAmount"
            } else {
                amountCategoryTV.text = formattedAmount
            }

            val drawable = ContextCompat.getDrawable(
                itemView.context,
                R.drawable.setting_background_item
            ) as GradientDrawable
            drawable.setColor(generateRandomLightColor())
        }

        private fun generateRandomLightColor(): Int {
            val random = java.util.Random()
            val baseColor =
                Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
            val lightColor = blendWithWhite(baseColor, 0.5f)
            return if (lightColor == Color.WHITE || lightColor == Color.BLACK) generateRandomLightColor() else lightColor
        }

        private fun blendWithWhite(color: Int, ratio: Float): Int {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val blendedRed = (red + (255 - red) * ratio).toInt()
            val blendedGreen = (green + (255 - green) * ratio).toInt()
            val blendedBlue = (blue + (255 - blue) * ratio).toInt()

            return Color.argb(255, blendedRed, blendedGreen, blendedBlue)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val title = dataList[position] as String
            val totals = totalsMap[title] ?: Totals()
            holder.bind(title, totals)
        } else if (holder is IncomeExpenseViewHolder) {
            holder.bind(dataList[position] as IncomeExpenseList)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
