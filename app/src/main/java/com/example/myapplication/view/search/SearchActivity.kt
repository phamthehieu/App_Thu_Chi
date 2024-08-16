package com.example.myapplication.view.search

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private var valueSearch: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNightMode()

        setupBackground()

        binding.textViewNumberDisplay.post {
            binding.textViewNumberDisplay.requestFocus()
        }
    }

    private fun setupBackground() {
        binding.textViewNumberDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                valueSearch = s.toString()
                if (valueSearch.isNotEmpty()) {
                    binding.iconEnd.visibility = View.VISIBLE
                } else {
                    binding.iconEnd.visibility = View.GONE
                }
            }
        })

        binding.iconEnd.setOnClickListener {
            binding.textViewNumberDisplay.text.clear()
        }
    }

    private fun setupNightMode() {

        val currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        binding.searchBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
        binding.resetBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
        binding.addIcon.setColorFilter(ContextCompat.getColor(this, R.color.black))

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
                binding.note.setBackgroundResource(R.drawable.background_search_white)
                binding.iconSearch.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.iconEnd.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.textViewNumberDisplay.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                val color = ContextCompat.getColor(this, R.color.gray7)
                this.window.statusBarColor = color
                binding.note.setBackgroundResource(R.drawable.background_search_black)
                binding.iconSearch.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.iconEnd.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.textViewNumberDisplay.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.gray7))
            }
        }
    }
}