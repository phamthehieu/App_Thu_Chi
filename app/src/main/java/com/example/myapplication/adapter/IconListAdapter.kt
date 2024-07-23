package com.example.myapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entity.Icon

class IconListAdapter(
    private val groupedIconMap: Map<String, List<Icon>>,
    private val iconSelectedListener: OnIconSelectedListener,
    private val selectedIcon: Int?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    interface OnIconSelectedListener {
        fun onIconSelected(icon: Icon)
    }


    private var selectedPosition = RecyclerView.NO_POSITION

    private val dataList: List<Any> = mutableListOf<Any>().apply {
        groupedIconMap.forEach { (type, icons) ->
            add(type)
            addAll(icons)
        }
    }

    init {
        if (dataList.isNotEmpty()) {
            if (selectedIcon != null) {
                selectedPosition = selectedIcon
            }
            notifyItemChanged(selectedPosition)
            iconSelectedListener.onIconSelected(dataList[selectedPosition] as Icon)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_item_list_icon, parent, false)
            IconViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(dataList[position] as String)
        } else if (holder is IconViewHolder) {
            holder.bind(dataList[position] as Icon, position == selectedPosition)
        }

        holder.itemView.setOnClickListener {
            if (holder is IconViewHolder) {
                notifyItemChanged(selectedPosition)
                selectedPosition = holder.adapterPosition
                notifyItemChanged(selectedPosition)
                iconSelectedListener.onIconSelected(dataList[position] as Icon)
            }
        }
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)

        fun bind(title: String) {
            val translatedTitle = when (title) {
                "Entertain" -> "Giải Trí"
                "Food" -> "Đồ ăn"
                "Shopping" -> "Mua sắm"
                "Left" -> "Cuộc sống"
                "Individual" -> "Cá nhân"
                "Education" -> "Giáo dục"
                "Festival" -> "Ngày hội"
                "Sport" -> "Thể thao"
                "Office" -> "Văn phòng"
                "Carriage" -> "Vận tải"
                "Health" -> "Sức khỏe"
                "Tourism" -> "Du lịch"
                "Finance" -> "Tài chính"
                "Other" -> "Khác"
                else -> title
            }

            headerTextView.text = translatedTitle
        }
    }

    class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.imageArtists)

        fun bind(icon: Icon, isSelected: Boolean) {
            iconImageView.setImageResource(icon.iconResource)
            if (isSelected) {
                itemView.isSelected = true
                iconImageView.setColorFilter(itemView.context.getColor(R.color.black))
            } else {
                itemView.isSelected = false
                iconImageView.clearColorFilter()
            }
        }
    }
}
