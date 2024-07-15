package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R

class PhotoAdapter( private val uris: List<Uri>, private val itemClickListenerPhoto: OnItemClickListenerPhoto) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_ADD_BUTTON = 1

    interface OnItemClickListenerPhoto {
        fun onItemClick()
        fun onDeleteItem(uri: Uri)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageArtists: ImageView = itemView.findViewById(R.id.imageArtists)
        val closeIcon: ImageView = itemView.findViewById(R.id.closeIcon)

        init {
            closeIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListenerPhoto.onDeleteItem(uris[position])
                }
            }
        }
    }


    inner class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnAdd: ImageView = itemView.findViewById(R.id.addPhotoBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_list_image, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_photo, parent, false)
            AddButtonViewHolder(view)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_ITEM) {
            val viewHolder = holder as ViewHolder
            val uri = uris[position]

                Glide.with(viewHolder.imageArtists.context)
                    .load(uri)
                    .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(56)))
                    .into(viewHolder.imageArtists)

            viewHolder.closeIcon.visibility = View.VISIBLE
        } else if (holder.itemViewType == VIEW_TYPE_ADD_BUTTON) {
            val addButtonViewHolder = holder as AddButtonViewHolder
            if (uris.size >= 3) {
                addButtonViewHolder.itemView.visibility = View.GONE
            } else {
                addButtonViewHolder.itemView.visibility = View.VISIBLE
                addButtonViewHolder.btnAdd.setOnClickListener {
                   itemClickListenerPhoto.onItemClick()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return uris.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < uris.size) {
            VIEW_TYPE_ITEM
        } else {
            VIEW_TYPE_ADD_BUTTON
        }
    }
}
