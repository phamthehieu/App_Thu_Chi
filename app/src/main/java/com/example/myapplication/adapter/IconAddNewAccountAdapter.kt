package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entity.Icon

class IconAddNewAccountAdapter(
    private val icons: List<Icon>,
    private val onIconClickListener: OnIconClickListener,
    private val selectedIcon: Icon? = null
) : RecyclerView.Adapter<IconAddNewAccountAdapter.IconViewHolder>() {

    private var selectedPosition = -1

    init {
        selectedIcon?.let { icon ->
            selectedPosition = icons.indexOfFirst { it.id == icon.id }
        }
    }

    interface OnIconClickListener {
        fun onIconClick(icon: Icon)
    }

    class IconViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById(R.id.imageArtists)
        val container: LinearLayout = view.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_icon_new_account, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val icon = icons[position]
        holder.iconImageView.setImageResource(icon.iconResource)

        if (selectedPosition == position) {
            holder.container.setBackgroundResource(R.drawable.background_item_new_account_selected)
        } else {
            holder.container.setBackgroundResource(R.drawable.background_icon_new_account)
        }

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            onIconClickListener.onIconClick(icon)
        }
    }

    override fun getItemCount(): Int {
        return icons.size
    }
}

