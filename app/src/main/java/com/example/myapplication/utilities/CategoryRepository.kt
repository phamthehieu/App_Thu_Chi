package com.example.myapplication.utilities

object CategoryRepository {
    private val selectedCategories = mutableListOf<Pair<Int, String>>()

    fun getCategories(): List<Pair<Int, String>> = selectedCategories

    fun addCategory(category: Pair<Int, String>) {
        if (selectedCategories.none { it.first == category.first }) {
            selectedCategories.add(category)
        }
    }

    fun removeCategoryById(id: Int) {
        selectedCategories.removeAll { it.first == id }
    }

    fun clearCategories() {
        selectedCategories.clear()
    }
}
