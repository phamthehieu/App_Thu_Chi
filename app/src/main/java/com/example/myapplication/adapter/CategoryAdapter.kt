package com.example.myapplication.adapter

import android.annotation.SuppressLint
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
    val itemClickListener: OnItemClickListener,
    private val isMultiSelectEnabled: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(category: CombinedCategoryIcon)
        fun onSettingsClick()
    }

    private val ITEM_VIEW_TYPE_CATEGORY = 0
    private val ITEM_VIEW_TYPE_SETTINGS = 1

    // Lưu các vị trí được chọn
    private val selectedPositions = mutableSetOf<Int>()

    // Vị trí của itemEdit (nếu có)
    private var editItemPosition: Int = -1

    override fun getItemViewType(position: Int): Int {
        return if (categories[position].categoryType == "setting") ITEM_VIEW_TYPE_SETTINGS else ITEM_VIEW_TYPE_CATEGORY
    }

    // Hàm để thiết lập vị trí của itemEdit
    fun setEditItemPosition(position: Int) {
        this.editItemPosition = position
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_SETTINGS) {
            (holder as SettingsViewHolder).bind()
        } else {
            // Kiểm tra xem item có được chọn hay không
            val isSelected = selectedPositions.contains(position) || position == editItemPosition
            (holder as CategoryViewHolder).bind(categories[position], isSelected)
        }

        holder.itemView.setOnClickListener {
            // Xử lý chọn item trong chế độ MultiSelect
            if (isMultiSelectEnabled) {
                if (selectedPositions.contains(holder.adapterPosition)) {
                    selectedPositions.remove(holder.adapterPosition)
                } else {
                    selectedPositions.add(holder.adapterPosition)
                }
            } else {
                // Chế độ chọn đơn
                selectedPositions.clear()
                selectedPositions.add(holder.adapterPosition)
            }

            notifyDataSetChanged()

            val item = categories[position]
            if (item.idCategory != -1) {
                itemClickListener.onItemClick(item)
            } else {
                itemClickListener.onSettingsClick()
            }
        }
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

    override fun getItemCount() = categories.size

    // ViewHolder cho các phần tử category
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.nameArtistsEt)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imageArtists)

        // Gán dữ liệu và thiết lập trạng thái đã chọn
        fun bind(category: CombinedCategoryIcon, isSelected: Boolean) {
            categoryName.text = category.categoryName
            categoryIcon.setImageResource(category.iconResource)

            // Thiết lập màu cho icon
            categoryIcon.setColorFilter(itemView.context.getColor(R.color.black))

            // Thay đổi nền khi được chọn
            itemView.isSelected = isSelected
        }
    }

    // ViewHolder cho item cài đặt
    class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val settingsText: TextView = itemView.findViewById(R.id.textSettings)
        private val settingsIcon: ImageView = itemView.findViewById(R.id.imageSettings)

        fun bind() {
            settingsText.text = "Cài đặt"
            settingsIcon.setImageResource(R.drawable.ic_add_24)
        }
    }
}
