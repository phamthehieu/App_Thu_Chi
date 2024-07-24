package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.IncomeExpenseListAdapter.OnItemClickListener
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.entity.Category
import com.example.myapplication.entity.IncomeExpenseList
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.DecimalFormat

data class CategoryWithIncomeExpenseList(
    val incomeExpense: IncomeExpenseList,
    val category: Category,
    var isSelected: Boolean = false
)

class DataListChartAdapter(
    private val items: List<CategoryWithIncomeExpenseList>,
    private val totalAmount: Float,
    private val itemClickListener: OnCategoryClickListener,
    private val checkDisplay: String
) : RecyclerView.Adapter<DataListChartAdapter.ChartViewHolder>() {

    interface OnCategoryClickListener {
        fun onItemClick(data: CategoryWithIncomeExpenseList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_item_chart, parent, false)
        return ChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, totalAmount, itemClickListener, checkDisplay)
    }

    override fun getItemCount(): Int = items.size

    class ChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconCategoryIV: ImageView = itemView.findViewById(R.id.iconCategoryIV)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val percentage: TextView = itemView.findViewById(R.id.percentage)
        private val amount: TextView = itemView.findViewById(R.id.amount)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val dateTv: TextView = itemView.findViewById(R.id.dateTv)

        @SuppressLint("SetTextI18n")
        fun bind(
            item: CategoryWithIncomeExpenseList,
            totalAmount: Float,
            itemClickListener: OnCategoryClickListener,
            checkDisplay: String
        ) {
            iconCategoryIV.setImageResource(item.incomeExpense.iconResource)
            iconCategoryIV.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black1))

            val amountString = item.incomeExpense.amount.replace(",", ".")
            val amountFloat = amountString.toFloatOrNull() ?: 0f

            val expenseFormatter = DecimalFormat("#,###.##")
            val formattedAmount = expenseFormatter.format(amountFloat)
            amount.text = formattedAmount

            val percentageValue = if (totalAmount != 0f) (amountFloat / totalAmount) * 100 else 0f
            val formattedPercentage = expenseFormatter.format(percentageValue)
            percentage.text = "$formattedPercentage%"
            progressBar.progress = percentageValue.toInt()

            if (checkDisplay == "chartFragment") {
                dateTv.visibility = View.GONE
                categoryName.text = item.category.name
            } else {
                dateTv.visibility = View.VISIBLE
                if (item.incomeExpense.note.isNotEmpty()) {
                    categoryName.text = item.incomeExpense.note
                } else {
                    categoryName.text = item.category.name
                }
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val dateFormat = LocalDate.parse(item.incomeExpense.date, formatter)
                val date = dateFormat.dayOfMonth
                val month = dateFormat.monthValue

                dateTv.text = "$date thg $month"
            }

            val drawable = ContextCompat.getDrawable(
                itemView.context,
                R.drawable.setting_background_item
            ) as GradientDrawable
            drawable.setColor(generateRandomLightColor())

            itemView.setOnClickListener {
                itemClickListener.onItemClick(item)
            }
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
}

