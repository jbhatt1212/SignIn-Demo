package com.example.signindemo

import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signindemo.databinding.ActivityMainBinding
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.initialize
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var mCredentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        FirebaseApp.initializeApp(this)
        setContentView(binding.root)
        mCredentialManager = CredentialManager.create(this)

        binding.btnSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }
        private fun signInWithGoogle() {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setNonce(generateNonce(32))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val cancellationSignal = CancellationSignal()

            mCredentialManager.getCredentialAsync(
                this,
                request,
                cancellationSignal,
                Executors.newSingleThreadExecutor(),
                object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                    override fun onResult(result: GetCredentialResponse) {
                        val credential = result.credential
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            firebaseAuthWithGoogle(idToken)
                        } else {
                            showMessage(binding.main, "Unexpected credential type")
                        }
                    }

                    override fun onError(e: GetCredentialException) {
                        handleCredentialError(e)
                    }
                }
            )
        }



        private fun handleCredentialError(e: GetCredentialException) {
            when (e) {
                is GetCredentialCancellationException -> showMessage(binding.main, "Sign-In was canceled by the user")
                is NoCredentialException -> showMessage(binding.main, "No credentials available")
                else -> showMessage(binding.main, "Error: ${e.message}")
            }
        }

        private fun firebaseAuthWithGoogle(idToken: String?) {
            if (idToken.isNullOrEmpty()) {
                showMessage(binding.main, "Invalid ID token")
                return
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        navigateToProfile()
                        Toast.makeText(this, "Sign-In Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        private fun navigateToProfile() {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        private fun generateNonce(length: Int): String {
            val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..length)
                .map { charset.random() }
                .joinToString("")
        }

        private fun showMessage(view: ViewGroup, message: String) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }
    }