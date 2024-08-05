package com.example.myapplication.view.reports

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        binding.addNewAccountBtn.setOnClickListener {
            val addNewAccountIntent = Intent(requireContext(), AddNewAccountActivity::class.java)
            startActivity(addNewAccountIntent)
        }

        return binding.root
    }

}