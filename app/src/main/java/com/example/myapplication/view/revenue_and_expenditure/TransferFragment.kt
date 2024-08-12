package com.example.myapplication.view.revenue_and_expenditure

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.databinding.FragmentTransferBinding
import com.example.myapplication.view.account.BottomSelectedAccountFragment

class TransferFragment : Fragment() {

    private lateinit var binding: FragmentTransferBinding

    private var account1: AccountIconFormat? = null

    private var account2: AccountIconFormat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransferBinding.inflate(inflater, container, false)

        binding.imageAccount1IV.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))

        binding.imageAccount2IV.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))

        binding.layoutAccount1.setOnClickListener {
            val keyboard = BottomSelectedAccountFragment()
            keyboard.show(childFragmentManager, "bottomSheetSelectedAccount")
        }

        binding.layoutAccount2.setOnClickListener {
            val keyboard = BottomSelectedAccountFragment()
            keyboard.show(childFragmentManager, "bottomSheetSelectedAccount")
        }


        return binding.root
    }

}