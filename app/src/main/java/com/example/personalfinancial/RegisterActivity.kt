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

public class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var passwordVisibilityImageView: ImageView
    private lateinit var confirmPasswordVisibilityImageView: ImageView
    private lateinit var boldLoginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        passwordVisibilityImageView = findViewById(R.id.passwordVisibilityImageView)
        confirmPasswordVisibilityImageView = findViewById(R.id.confirmPasswordVisibilityImageView)
        boldLoginTextView = findViewById(R.id.boldLoginTextView)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password == confirmPassword) {
                // Make API call to register user
                registerUser(username, email, password)
            } else {
                Toast.makeText(applicationContext, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
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

        confirmPasswordVisibilityImageView.setOnClickListener {

            if (confirmPasswordEditText.text.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter confirm password first", Toast.LENGTH_SHORT).show()
            } else {
                val isPasswordVisible = confirmPasswordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                if (isPasswordVisible) {
                    // Hide the password
                    confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    confirmPasswordVisibilityImageView.setImageResource(R.drawable.visibility_icon)
                } else {
                    // Show the password
                    confirmPasswordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    confirmPasswordVisibilityImageView.setImageResource(R.drawable.visibility_off_icon)
                }

                // Move the cursor to the end of the password
                confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
            }
        }

        boldLoginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2:8080/auth/register"

        val requestBody = JSONObject()
            .put("username", username)
            .put("email", email)
            .put("password", password)
            .toString()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to connect to the server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Registration successful", Toast.LENGTH_SHORT).show()
                        // Redirect to the login activity or perform necessary actions
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
