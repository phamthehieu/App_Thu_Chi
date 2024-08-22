package com.example.myapplication.view.user

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentUserBinding
import com.example.myapplication.utilities.DatabaseSyncHelper
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import com.example.myapplication.viewModel.HistoryAccountViewModel
import com.example.myapplication.viewModel.HistoryAccountViewModelFactory
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.*


class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val accountViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(requireActivity().application)
    }

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(requireActivity().application)
    }

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(requireActivity().application)
    }

    private val historyAccountViewModel: HistoryAccountViewModel by viewModels {
        HistoryAccountViewModelFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureGoogleSignIn()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        setupBackground()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupBackground()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupBackground() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            if (account.photoUrl != null) {
                Glide.with(this)
                    .load(account.photoUrl)
                    .into(binding.profileIv)
                binding.profileIv.visibility = View.VISIBLE
                binding.profileTv.visibility = View.GONE
                binding.nameUser.text = account.displayName
                binding.email.text = account.email
            } else {
                val displayName = account.displayName ?: "?"
                val firstLetter = displayName.first().uppercaseChar()
                binding.profileIv.setImageResource(0)
                binding.profileTv.text = firstLetter.toString()
                binding.profileIv.visibility = View.GONE
                binding.profileTv.visibility = View.VISIBLE
                binding.nameUser.text = account.displayName
                binding.email.text = account.email
            }
        } else {
            binding.nameUser.text = "Đăng nhập"
            binding.nameUser.setTextColor(resources.getColor(R.color.yellow))
            binding.email.text = "Đăng nhập, thú vị hơn"
            binding.profileIv.setImageResource(R.drawable.ic_person_24)
            binding.profileIv.visibility = View.VISIBLE
            binding.profileTv.visibility = View.GONE
        }

        binding.backBtn.setOnClickListener {
            googleSignInClient.signOut()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        setupBackground()
                        lifecycleScope.launch {
                            accountViewModel.deleteAllAccount()
                            incomeExpenseListModel.deleteAllTable()
                            categoryViewModel.deleteAllCategoryWithType()
                            historyAccountViewModel.deleteAllHistoryAccount()
                        }
                    } else {
                        Log.e("UserFragment", "Sign-out failed")
                    }
                }
        }

        binding.profile.setOnClickListener {
            if (account == null) {
                val login = LoginAccountGoogleFragment()
                login.show(childFragmentManager, "loginGoogle")
            }
        }

        binding.dataSynchronization.setOnClickListener {
            if (account == null) {
                val login = LoginAccountGoogleFragment()
                login.show(childFragmentManager, "loginGoogle")
            } else {
                lifecycleScope.launch {
                    val driveService = getDriveService()
                    if (driveService != null) {
                        val snackbar = Snackbar.make(
                            requireView(),
                            "Đang đồng bộ hóa dữ liệu...",
                            Snackbar.LENGTH_INDEFINITE
                        )

                        val snackbarView = snackbar.view
                        val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
                        params.gravity = Gravity.TOP
                        snackbarView.layoutParams = params

                        snackbar.show()

                        val success = try {
                            DatabaseSyncHelper.syncDatabaseWithDriveAll(driveService, requireContext())
                            true
                        } catch (e: Exception) {
                            Log.e("UserFragment", "Sync failed", e)
                            false
                        }
                        snackbar.dismiss()
                        val resultMessage = if (success) {
                            "Đồng bộ hóa thành công!"
                        } else {
                            "Đồng bộ hóa thất bại. Vui lòng thử lại."
                        }
                        Snackbar.make(
                            requireView(),
                            resultMessage,
                            Snackbar.LENGTH_SHORT
                        ).apply {
                            val resultSnackbarView = view
                            val resultParams = resultSnackbarView.layoutParams as CoordinatorLayout.LayoutParams
                            resultParams.gravity = Gravity.TOP
                            resultSnackbarView.layoutParams = resultParams
                            show()
                        }
                    } else {
                        Log.e("UserFragment", "Drive service is null")
                    }
                }
            }
        }


    }

    private fun getDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        return account?.let {
            val credential = GoogleAccountCredential.usingOAuth2(
                requireContext(), listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val transport = NetHttpTransport()

            Drive.Builder(
                transport,
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(getString(R.string.app_name)).build()
        }
    }

}

