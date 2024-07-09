package com.example.myapplication.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.CombinedCategoryIcon

class SettingCategoryAdapter(
    private val categories: List<CombinedCategoryIcon>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<SettingCategoryAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(category: CombinedCategoryIcon)
        fun editCategory(category: CombinedCategoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_setting_category_item, parent, false)
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
        private val editCategory: ImageView = itemView.findViewById(R.id.editCategory)
        private val deleteCategory: ImageView = itemView.findViewById(R.id.deleteCategory)

        init {
            deleteCategory.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = categories[position]
                    itemClickListener.onItemClick(category)
                }
            }

            editCategory.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = categories[position]
                    itemClickListener.editCategory(category)
                }
            }
        }

        fun bind(category: CombinedCategoryIcon) {
            categoryName.text = category.categoryName
            categoryIcon.setImageResource(category.iconResource)

            categoryIcon.setColorFilter(Color.BLACK)
            val drawable =
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.setting_background_item
                ) as GradientDrawable
            drawable.setColor(generateRandomLightColor())

            if (category.categoryType == "admin") {
                editCategory.visibility = View.GONE
            } else {
                editCategory.visibility = View.VISIBLE
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
