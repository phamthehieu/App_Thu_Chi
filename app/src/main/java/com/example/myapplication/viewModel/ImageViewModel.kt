package com.example.myapplication.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    private val _imageUris = MutableLiveData<MutableList<Uri>>(mutableListOf())
    val imageUris: LiveData<MutableList<Uri>> get() = _imageUris

    fun addImageUri(uri: Uri) {
        _imageUris.value?.add(uri)
        _imageUris.value = _imageUris.value
    }

    fun removeImageUri(uri: Uri) {
        _imageUris.value?.remove(uri)
        _imageUris.value = _imageUris.value
    }

    fun updateImageUri(oldUri: Uri, newUri: Uri) {
        val index = _imageUris.value?.indexOf(oldUri)
        if (index != null && index >= 0) {
            _imageUris.value?.set(index, newUri)
            _imageUris.value = _imageUris.value
        }
    }

    fun clearImageUris() {
        _imageUris.value?.clear()
        _imageUris.value = _imageUris.value
    }

}
