package com.example.myapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.CombinedCategoryIcon

class CategoryAdapter(
    private val categories: List<CombinedCategoryIcon>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(category: CombinedCategoryIcon)
        fun onSettingsClick()
    }

    private val ITEM_VIEW_TYPE_CATEGORY = 0
    private val ITEM_VIEW_TYPE_SETTINGS = 1

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun getItemViewType(position: Int): Int {
        return if (categories[position].categoryType == "setting") ITEM_VIEW_TYPE_SETTINGS else ITEM_VIEW_TYPE_CATEGORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_VIEW_TYPE_SETTINGS) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
            SettingsViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_category_item, parent, false)
            CategoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_SETTINGS) {
            (holder as SettingsViewHolder).bind()
        } else {
            (holder as CategoryViewHolder).bind(categories[position], position == selectedPosition)
        }
        holder.itemView.setOnClickListener {
            Log.d("CategoryViewHolder", "${categories[position]}")
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            val item = categories[position]
            if (item.idCategory != -1) {
                itemClickListener.onItemClick(item)
            } else {
                itemClickListener.onSettingsClick()
            }
        }
    }

    override fun getItemCount() = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.nameArtistsEt)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imageArtists)

        fun bind(category: CombinedCategoryIcon, isSelected: Boolean) {
            categoryName.text = category.categoryName
            categoryIcon.setImageResource(category.iconResource)
            if (isSelected) {
                itemView.isSelected = true
                categoryIcon.setColorFilter(itemView.context.getColor(R.color.black))
            } else {
                itemView.isSelected = false
                categoryIcon.clearColorFilter()
            }
        }
    }

    class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val settingsText: TextView = itemView.findViewById(R.id.textSettings)
        private val settingsIcon: ImageView = itemView.findViewById(R.id.imageSettings)

        fun bind() {
            settingsText.text = "Cài đặt"
            settingsIcon.setImageResource(R.drawable.ic_add_24)
        }
    }
}