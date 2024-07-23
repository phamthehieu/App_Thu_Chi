package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import java.text.DecimalFormat

class DataListChartAdapter(
    private val items: List<CategoryWithIncomeExpenseList>,
    private val totalAmount: Float
) : RecyclerView.Adapter<DataListChartAdapter.ChartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_item_chart, parent, false)
        return ChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, totalAmount)
    }

    override fun getItemCount(): Int = items.size

    class ChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconCategoryIV: ImageView = itemView.findViewById(R.id.iconCategoryIV)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val percentage: TextView = itemView.findViewById(R.id.percentage)
        private val amount: TextView = itemView.findViewById(R.id.amount)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        @SuppressLint("SetTextI18n")
        fun bind(item: CategoryWithIncomeExpenseList, totalAmount: Float) {
            iconCategoryIV.setImageResource(item.incomeExpense.iconResource)
            categoryName.text = item.category.name
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
}
