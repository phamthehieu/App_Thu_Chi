package com.example.myapplication.utilities

interface ItemMoveCallback {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}
