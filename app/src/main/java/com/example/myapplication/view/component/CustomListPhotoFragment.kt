package com.example.myapplication.view.component

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.PhotoAdapter
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.FragmentCustomListPhotoBinding
import com.example.myapplication.entity.Category
import com.example.myapplication.viewModel.ImageViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CustomListPhotoFragment : BottomSheetDialogFragment(), PhotoAdapter.OnItemClickListenerPhoto {

    private lateinit var binding: FragmentCustomListPhotoBinding

    private val imageViewModel: ImageViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(resources.getColor(R.color.black, null))

            bottomSheet?.viewTreeObserver?.addOnGlobalLayoutListener {
                val rect = android.graphics.Rect()
                bottomSheet.getWindowVisibleDisplayFrame(rect)
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomListPhotoBinding.inflate(inflater, container, false)
        val recyclerView = binding.recyclerViewCustomListPhoto
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        imageViewModel.imageUris.observe(viewLifecycleOwner, Observer { uris ->
            recyclerView.adapter = PhotoAdapter(uris, this)
        })

        binding.acceptBtn.setOnClickListener {
            dismiss()
        }

        binding.removeAllBtn.setOnClickListener {
            imageViewModel.clearImageUris()
            dismiss()
        }

        return binding.root
    }

    override fun onItemClick() {
        dismiss()
        val bottomSheet = BottomSheetSelectedImageFragment()
        fragmentManager?.let { bottomSheet.show(it, "bottomSheetSelectedImage") }
    }

    override fun onDeleteItem(uri: Uri) {
        context.let { cxt ->
            val dialog = Dialog(cxt!!)
            dialog.setContentView(R.layout.layout_confirm_delete)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val backDeleteBtn = dialog.findViewById<View>(R.id.backDeleteBtn)
            val successDeleteBtn = dialog.findViewById<View>(R.id.successDeleteBtn)

            backDeleteBtn.setOnClickListener {
                dialog.dismiss()
            }

            successDeleteBtn.setOnClickListener {
                imageViewModel.removeImageUri(uri)
                dialog.dismiss()
            }

            dialog.show()
        }

    }
}