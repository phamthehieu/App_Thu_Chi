package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class CategoriesSearchAdapter(
    private var categories: List<Pair<Int, String>>,
    private val onItemClickListener: OnItemClickListenerSearch
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_ADD_BUTTON = 1
        private const val VIEW_TYPE_ALL_CATEGORY = 2
    }

    interface OnItemClickListenerSearch {
        fun onDeleteItem(category: Pair<Int, String>)
        fun onAddButtonClick()
        fun onAllCategoryClick()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameCategorySearch: TextView = itemView.findViewById(R.id.nameCategorySearch)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
    }

    inner class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnAdd: RelativeLayout = itemView.findViewById(R.id.addBtn)
        val addIcon: ImageView = itemView.findViewById(R.id.addIcon)
    }

    inner class AllCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val allCategory: RelativeLayout = itemView.findViewById(R.id.allCategory)
        val allCategoryText: TextView = itemView.findViewById(R.id.allCategoryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_reycler_view_search, parent, false)
                ViewHolder(view)
            }
            VIEW_TYPE_ADD_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_new_category_search, parent, false)
                AddButtonViewHolder(view)
            }
            VIEW_TYPE_ALL_CATEGORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_all_category_btn, parent, false)
                AllCategoryViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_ITEM -> {
                val viewHolder = holder as ViewHolder
                val category = categories[position - 1]
                viewHolder.nameCategorySearch.text = category.second
                viewHolder.deleteIcon.setOnClickListener {
                    onItemClickListener.onDeleteItem(category)
                }
            }
            VIEW_TYPE_ADD_BUTTON -> {
                val addButtonViewHolder = holder as AddButtonViewHolder
                addButtonViewHolder.addIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
                addButtonViewHolder.btnAdd.setOnClickListener {
                    onItemClickListener.onAddButtonClick()
                }
            }
            VIEW_TYPE_ALL_CATEGORY -> {
                val allCategoryViewHolder = holder as AllCategoryViewHolder
                if(categories.isNotEmpty()) {
                    allCategoryViewHolder.allCategory.setBackgroundResource(R.drawable.background_gray)
                    allCategoryViewHolder.allCategoryText.setTextColor(holder.itemView.context.getColor(R.color.white))
                } else {
                    allCategoryViewHolder.allCategory.setBackgroundResource(R.drawable.background_yellow)
                    allCategoryViewHolder.allCategoryText.setTextColor(holder.itemView.context.getColor(R.color.black))
                }
                allCategoryViewHolder.allCategoryText.text = "Tất cả"
                allCategoryViewHolder.allCategory.setOnClickListener {
                    onItemClickListener.onAllCategoryClick()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return categories.size + 2
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newCategories: List<Pair<Int, String>>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_ALL_CATEGORY
            in 1 until categories.size + 1 -> VIEW_TYPE_ITEM
            else -> VIEW_TYPE_ADD_BUTTON
        }
    }
}
