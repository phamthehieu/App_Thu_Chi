package com.example.myapplication.view.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginAccountGoogleBinding
import com.example.myapplication.utilities.DatabaseSyncHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.launch

class LoginAccountGoogleFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentLoginAccountGoogleBinding
    private val RC_SIGN_IN = 9001
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureGoogleSignIn()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginAccountGoogleBinding.inflate(inflater, container, false)

        setupViews()

        return binding.root
    }

    private fun setupViews() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.googleBtn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        binding.layoutLogin.visibility = View.GONE
        binding.layoutLoading.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    lifecycleScope.launch {
                        val driveService = getDriveService()
                        if (driveService != null) {
                            DatabaseSyncHelper.downloadAndRestoreDatabase(driveService, requireContext())
                            binding.layoutLogin.visibility = View.VISIBLE
                            binding.layoutLoading.visibility = View.GONE
                            dismiss()
                        } else {
                            Log.e("UserFragment", "Drive service is null")
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.w("LoginAccountGoogleFragment", "Google sign in failed", e)
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

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.9).toInt()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}
