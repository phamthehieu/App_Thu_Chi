package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.Example5EventItemViewBinding
import com.example.myapplication.entity.IncomeExpenseList
import java.text.DecimalFormat

class CategoryListAdapter : RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder>() {
    private val categories = mutableListOf<IncomeExpenseList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = Example5EventItemViewBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
        viewHolder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun submitList(newCategories: List<IncomeExpenseList>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(private val binding: Example5EventItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(data: IncomeExpenseList) {
            binding.iconCategoryCalendarIV.setImageResource(data.iconResource)
            binding.iconCategoryCalendarIV.setColorFilter(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.black
                )
            )
            val drawable =
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.setting_background_item
                ) as GradientDrawable
            drawable.setColor(generateRandomLightColor())

            val amount = data.amount.replace(",", ".").toDouble()
            val expenseFormatter = DecimalFormat("#,###.##")
            val formattedAmount = expenseFormatter.format(amount)

            if (data.type == "Expense") {
                binding.amountCategoryCalendarTV.text = "-$formattedAmount"
            } else {
                binding.amountCategoryCalendarTV.text = formattedAmount
            }
            binding.nameCategoryCalendarTV.text = data.categoryName
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
