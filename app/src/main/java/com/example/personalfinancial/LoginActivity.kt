package com.example.personalfinancial

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var boldSignupTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var passwordVisibilityImageView: ImageView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        boldSignupTextView = findViewById(R.id.backToSignUpTextView)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        passwordVisibilityImageView = findViewById(R.id.passwordVisibilityImageView)

        sharedPreferences = getSharedPreferences("Myprefs", Context.MODE_PRIVATE)

            // Redirect to the main activity when authToken not null
            val storedAuthToken = sharedPreferences.getString("authToken", null)
            if (storedAuthToken != null) {
                // Redirect to the home activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish() // Optional: finish the current activity to prevent the user from going back
            }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
//            val username = "aaa"
//            val password = "123"

            // Make API call to authenticate user
            authenticateUser(username, password)
        }

        passwordVisibilityImageView.setOnClickListener {

            if (usernameEditText.text.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter password first", Toast.LENGTH_SHORT).show()
            } else {
                val isPasswordVisible = passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                if (isPasswordVisible) {
                    // Hide the password
                    passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    passwordVisibilityImageView.setImageResource(R.drawable.visibility_icon)
                } else {
                    // Show the password
                    passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    passwordVisibilityImageView.setImageResource(R.drawable.visibility_off_icon)
                }

                // Move the cursor to the end of the password
                passwordEditText.setSelection(passwordEditText.text.length)
            }
        }

        boldSignupTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordTextView.setOnClickListener{
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun authenticateUser(username: String, password: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2:8080/auth/login"

//        val requestBody = FormBody.Builder()
//            .add("username", username)
//            .add("password", password)
//            .build()
//
//        val request = Request.Builder()
//            .url(url)
//            .post(requestBody)
//            .build()

        val requestBody = JSONObject()
            .put("username", username)
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
                val token = response.body?.string()
                // Store the authentication token in SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("authToken", token)
                editor.putString("username", username)
                editor.apply()

                if (response.isSuccessful) {
                    // User authenticated successfully
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Login successful", Toast.LENGTH_SHORT).show()
                        // Redirect to the main activity or perform necessary actions
                        val storedAuthToken = sharedPreferences.getString("authToken", null)
                        if (storedAuthToken != null) {
                            // Redirect to the home activity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            storedUserId(username, token!!)
                            finish() // Optional: finish the current activity to prevent the user from going back
                        }

                    }
                } else {
                    // User authentication failed
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun storedUserId(username: String, authToken: String) {
        val client = OkHttpClient()
        // Fetch user id from API
        val requestUserId = Request.Builder()
            .url("http://10.0.2.2:8080/auth/users/$username/id")
            .header("Authorization", "Bearer $authToken")
            .build()

        client.newCall(requestUserId).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val userId = response.body?.string()
                    val editor = sharedPreferences.edit()
                    editor.putString("user_id", userId)
                    editor.apply()
                }
            }
        })
    }
}
