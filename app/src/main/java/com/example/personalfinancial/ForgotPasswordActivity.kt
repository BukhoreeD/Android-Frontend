package com.example.personalfinancial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var backToSignInTextView: TextView
    private  lateinit var backToSignUpTextView: TextView
    private lateinit var emailEditText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        backToSignInTextView = findViewById(R.id.backToSignInTextView)
        backToSignUpTextView = findViewById(R.id.backToSignUpTextView)
        emailEditText = findViewById(R.id.emailEditText)
        sendButton = findViewById(R.id.sendButton)

        backToSignInTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        sendButton.setOnClickListener {
            sendResetPasswordEmail()
        }

        backToSignUpTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun sendResetPasswordEmail() {
        val email = emailEditText.text.toString()

        val client = OkHttpClient()
        val url = "http://10.0.2.2:8080/auth/forgot-password"

        val requestBody = JSONObject()
            .put("email", email)
            .toString()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle API call failure
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to connect to the server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // User authenticated successfully
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Reset password email sent to $email", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, VerificationActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    }
                } else {
                    // User authentication failed
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to sent reset password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
