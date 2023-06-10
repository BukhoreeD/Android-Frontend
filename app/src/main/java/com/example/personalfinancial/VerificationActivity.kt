package com.example.personalfinancial

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

class VerificationActivity : AppCompatActivity() {
    private lateinit var sendButton: Button
    private lateinit var resetCodeEditText: EditText
    private lateinit var resendCodeTextView: TextView
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var newPasswordVisibilityImageView: ImageView
    private lateinit var confirmNewPasswordVisibilityImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        sendButton = findViewById(R.id.sendButton)
        resetCodeEditText = findViewById(R.id.resetCodeEditText)
        resendCodeTextView = findViewById(R.id.resendCodeTextView)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText)
        newPasswordVisibilityImageView = findViewById(R.id.newPasswordVisibilityImageView)
        confirmNewPasswordVisibilityImageView = findViewById(R.id.confirmNewPasswordVisibilityImageView)

        val email = intent.getStringExtra("email")

        newPasswordVisibilityImageView.setOnClickListener {

            if (newPasswordEditText.text.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter new password first", Toast.LENGTH_SHORT).show()
            } else {
                val isPasswordVisible = newPasswordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                if (isPasswordVisible) {
                    // Hide the password
                    newPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    newPasswordVisibilityImageView.setImageResource(R.drawable.visibility_icon)
                } else {
                    // Show the password
                    newPasswordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    newPasswordVisibilityImageView.setImageResource(R.drawable.visibility_off_icon)
                }

                // Move the cursor to the end of the password
                newPasswordEditText.setSelection(newPasswordEditText.text.length)
            }
        }

        confirmNewPasswordVisibilityImageView.setOnClickListener {

            if (confirmNewPasswordEditText.text.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter confirm new password first", Toast.LENGTH_SHORT).show()
            } else {
                val isPasswordVisible = confirmNewPasswordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                if (isPasswordVisible) {
                    // Hide the password
                    confirmNewPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    confirmNewPasswordVisibilityImageView.setImageResource(R.drawable.visibility_icon)
                } else {
                    // Show the password
                    confirmNewPasswordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    confirmNewPasswordVisibilityImageView.setImageResource(R.drawable.visibility_off_icon)
                }

                // Move the cursor to the end of the password
                confirmNewPasswordEditText.setSelection(confirmNewPasswordEditText.text.length)
            }
        }

        sendButton.setOnClickListener {
            // Handle send button click event
            val resetCode = resetCodeEditText.text.toString()
            val password = newPasswordEditText.text.toString()
            val confirmPassword = confirmNewPasswordEditText.text.toString()

            if (password == confirmPassword) {
                // Make API call to register user
                verifyResetCode(email!!, resetCode, password)
            } else {
                Toast.makeText(applicationContext, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        resendCodeTextView.setOnClickListener {
            // Handle re-send code click event
            resendVerificationCode(email!!)
        }
    }

    private fun verifyResetCode(email: String, resetCode: String, password: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2:8080/auth/reset-password"

        val requestBody = JSONObject()
            .put("email", email)
            .put("resetCode", resetCode)
            .put("password", password)
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
                        Toast.makeText(applicationContext, "Password has been reset", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // User authentication failed
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to  reset password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun resendVerificationCode(email: String) {
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
                        Toast.makeText(applicationContext, "Resend verification code", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User authentication failed
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to resent reset password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
