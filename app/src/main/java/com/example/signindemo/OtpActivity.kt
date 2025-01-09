package com.example.signindemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signindemo.databinding.ActivityOtpBinding
import com.example.signindemo.databinding.ActivityPhoneNumberBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
        setContentView(binding.root)

      auth = FirebaseAuth.getInstance()
        val storedVerificationId= intent.getStringExtra("storedVerificationId")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnLogin.setOnClickListener{
            if (binding.etOtp.text.trim().toString().isNotEmpty()){
                val credential:PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(),binding.etOtp.text.trim().toString()
                )
                signInWithPhoneAuthCredential(credential)
            }else{
                Toast.makeText(this,"Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity2::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.e("otp invalid","error : ${task.exception}")
                        Toast.makeText(this,"Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }
}