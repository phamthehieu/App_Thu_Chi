package com.example.myapplication.view.reports

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentReportsBinding

class ReportsFragment : Fragment() {

    private lateinit var binding: FragmentReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportsBinding.inflate(inflater, container, false)

        val progressBar = binding.progressBarCircular
        val progressText = binding.progressText

        val progress = 4.05
        progressBar.progress = progress.toInt()
        progressText.text = "${progress.toInt()}%"

        binding.budgetTotal.setOnClickListener {
            val budgetViewIntent = Intent(requireContext(), BudgetViewActivity::class.java)
            startActivity(budgetViewIntent)
        }

        return binding.root
    }

}