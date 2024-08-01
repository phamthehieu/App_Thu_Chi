package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.CombinedCategoryReport
import java.text.DecimalFormat

class ListDetailBudgetAdapter(
    private val combinedList: List<CombinedCategoryReport>,
    private val itemClickListener: OnItemClickListenerDetail

): RecyclerView.Adapter<ListDetailBudgetAdapter.ViewHolder>() {

    interface OnItemClickListenerDetail {
        fun editBudget(category: CombinedCategoryReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget_cateogry, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int {
        return combinedList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(combinedList[position])
    }

    inner class ViewHolder(
        itemView: View,
        private val itemClickListener: OnItemClickListenerDetail
    ) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.nameCategoryTV)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.iconCategoryIV)
        private val editBudget: TextView = itemView.findViewById(R.id.editBudget)
        private val totalCategoryTV: TextView = itemView.findViewById(R.id.totalCategoryTV)
        private val textBudget: TextView = itemView.findViewById(R.id.textBudget)
        private val remainingTv: TextView = itemView.findViewById(R.id.remainingTv)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarCircular)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)

        init {

            editBudget.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = combinedList[position]
                    itemClickListener.editBudget(category)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(combinedList: CombinedCategoryReport) {

            categoryName.text = combinedList.categoryName

            categoryIcon.setImageResource(combinedList.iconResource)

            val budgetString = combinedList.budget.replace(',', '.')
            val budgetValue = budgetString.toDoubleOrNull() ?: 0.0
            val totalString = combinedList.totalAmount.replace(',', '.')
            val totalValue = totalString.toDoubleOrNull() ?: 0.0
            val totalCategoryString = combinedList.totalAmount.replace(',', '.')
            val totalCategoryValue = totalCategoryString.toDoubleOrNull() ?: 0.0
            val decimalFormat = DecimalFormat("#,###.##")

            val remaining = budgetValue - totalValue

            val remainingPercentage = if (budgetValue != 0.0) {
                (remaining / budgetValue) * 100
            } else {
                0.0
            }

            remainingTv.text = decimalFormat.format(remaining)
            totalCategoryTV.text = decimalFormat.format(totalCategoryValue)
            textBudget.text = decimalFormat.format(budgetValue)

            if (remainingPercentage > 0) {
                progressBar.progress = remainingPercentage.toInt()
                progressText.text = "${decimalFormat.format(remainingPercentage)}%"
            } else {
                progressBar.progress = 0
                progressText.text = "__"
            }

            categoryIcon.setColorFilter(Color.BLACK)
            val drawable =
                ContextCompat.getDrawable(
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