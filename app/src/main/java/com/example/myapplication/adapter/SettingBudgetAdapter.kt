package com.example.myapplication.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.SettingCategoryAdapter.OnItemClickListener
import com.example.myapplication.data.CombinedCategoryIcon
import java.text.DecimalFormat

class SettingBudgetAdapter(
    private val categories: List<CombinedCategoryIcon>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SettingBudgetAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(category: CombinedCategoryIcon)
        fun editBudget(category: CombinedCategoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_add_new_budget, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class ViewHolder(
        itemView: View,
        private val itemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.nameCategory)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.iconCategory)
        private val editBudget: LinearLayout = itemView.findViewById(R.id.editBudget)
        private val deleteCategory: ImageView = itemView.findViewById(R.id.deleteCategory)
        private val totalBudget: TextView = itemView.findViewById(R.id.totalBudget)

        init {
            deleteCategory.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = categories[position]
                    val budgetString = category.budget.replace(',', '.')
                    val budgetValue = budgetString.toDoubleOrNull() ?: 0.0
                    if (budgetValue != 0.0) {
                        itemClickListener.onItemClick(category)
                    }
                }
            }

            editBudget.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = categories[position]
                    itemClickListener.editBudget(category)
                }
            }
        }

        fun bind(category: CombinedCategoryIcon) {
            categoryName.text = category.categoryName
            categoryIcon.setImageResource(category.iconResource)

            val budgetString = category.budget.replace(',', '.')
            val budgetValue = budgetString.toDoubleOrNull() ?: 0.0
            val decimalFormat = DecimalFormat("#,###.##")
            if (budgetValue == 0.0) {
                deleteCategory.setImageResource(R.drawable.ic_minus_notselected_24)
            } else {
                deleteCategory.setImageResource(R.drawable.ic_minus_24)
                totalBudget.text = decimalFormat.format(budgetValue)
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