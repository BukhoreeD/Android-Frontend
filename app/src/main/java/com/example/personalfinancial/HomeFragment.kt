package com.example.personalfinancial

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException

class HomeFragment : Fragment() {
    // Views
    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var incomeButton: Button
    private lateinit var expenseButton: Button
    private lateinit var incomeCategories: List<String>
    private lateinit var expenseCategories: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        imageView = view.findViewById(R.id.imageView)
        titleTextView = view.findViewById(R.id.titleTextView)
        incomeButton = view.findViewById(R.id.incomeButton)
        expenseButton = view.findViewById(R.id.expenseButton)

        // Set up click listener for the "Income" button
        incomeButton.setOnClickListener {
            showAddIncomeDialog()
        }

        expenseButton.setOnClickListener {
            showAddExpenseDialog()
        }

        // Fetch categories
        fetchCategories()

        return view
    }

    private fun showAddIncomeDialog() {
        // Create a custom dialog
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_add_income)

        // Initialize dialog views
        val amountEditText = dialog.findViewById<EditText>(R.id.amountEditText)
        val categorySpinner = dialog.findViewById<Spinner>(R.id.categorySpinner)
        val noteEditText = dialog.findViewById<EditText>(R.id.noteEditText)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val okButton = dialog.findViewById<Button>(R.id.okButton)

        // Set up click listener for the "Cancel" button
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Set up click listener for the "OK" button
        okButton.setOnClickListener {
            val amount = amountEditText?.text.toString()
            val category = categorySpinner?.selectedItem.toString()
            val note = noteEditText?.text.toString()

            createIncomeTransaction(amount, category, note)

            dialog.dismiss()
        }

        // Filter categories by type "income"
        val filteredCategories = incomeCategories

        // Populate the categorySpinner with the filtered categories
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filteredCategories
        )
        categorySpinner.adapter = adapter

        dialog.show()
    }

    private fun showAddExpenseDialog() {
        // Create a custom dialog
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_add_expense)

        // Initialize dialog views
        val amountEditText = dialog.findViewById<EditText>(R.id.amountEditText)
        val categorySpinner = dialog.findViewById<Spinner>(R.id.categorySpinner)
        val noteEditText = dialog.findViewById<EditText>(R.id.noteEditText)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val okButton = dialog.findViewById<Button>(R.id.okButton)

        // Set up click listener for the "Cancel" button
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Set up click listener for the "OK" button
        okButton.setOnClickListener {
            val amount = amountEditText?.text.toString()
            val category = categorySpinner?.selectedItem.toString()
            val note = noteEditText?.text.toString()

            createExpenseTransaction(amount, category, note)

            dialog.dismiss()
        }

        // Filter categories by type "expense"
        val filteredCategories = expenseCategories

        // Populate the categorySpinner with the filtered categories
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filteredCategories
        )
        categorySpinner.adapter = adapter

        dialog.show()
    }

    private fun fetchCategories() {
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/categories")
            .header("Authorization", "Bearer $authToken")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                activity?.runOnUiThread {
                    // Update UI accordingly
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("TAG", "onResponse: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    incomeCategories = parseIncomeCategories(responseBody)
                    expenseCategories = parseExpenseCategories(responseBody)
                } else {
                    // Handle unsuccessful response
                    activity?.runOnUiThread {
                        // Update UI accordingly
                    }
                }
            }
        })
    }

    private fun parseIncomeCategories(responseBody: String): List<String> {
        val incomeCategories = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val category = jsonArray.getJSONObject(i)
                val categoryName = category.getString("categoryName")
                val type = category.getJSONObject("type")
                val typeName = type.getString("typeName")
                if (typeName == "income") {
                    incomeCategories.add(categoryName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return incomeCategories
    }

    private fun parseExpenseCategories(responseBody: String): List<String> {
        val expenseCategories = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val category = jsonArray.getJSONObject(i)
                val categoryName = category.getString("categoryName")
                val type = category.getJSONObject("type")
                val typeName = type.getString("typeName")
                if (typeName == "expense") {
                    expenseCategories.add(categoryName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return expenseCategories
    }

    private fun getAuthTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("authToken", null)
    }

    private fun createIncomeTransaction(amount: String, category: String, note: String) {
        val sharedPreferences = requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        // Create the request body
        val currentDate = LocalDate.now()
        val formattedDate = currentDate.format(DateTimeFormatter.ISO_DATE)
        val requestBody = JSONObject().apply {
            put("transactionDate", formattedDate)
            put("transactionAmount", amount.toDouble())
            put("transactionNote", note)
            put("category", JSONObject().apply {
                put("categoryId", (incomeCategories.indexOf(category)+1))
                put("categoryName", category)
            })
            put("type", JSONObject().apply {
                put("typeId", 1)
                put("typeName", "income")
            })
            put("user", JSONObject().apply {
                put("userId", sharedPreferences.getString("user_id", null))
                put("username", sharedPreferences.getString("username", null))
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/transactions")
            .header("Authorization", "Bearer $authToken")
            .post(requestBody.toString().toRequestBody(mediaType))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                activity?.runOnUiThread {
                    // Update UI accordingly
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("TAG", "onResponse: $responseBody")
                if (response.isSuccessful && responseBody != null) {
                    // Success response, handle accordingly
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Transaction has been saved", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle unsuccessful response
                    activity?.runOnUiThread {
                        // Update UI accordingly
                    }
                }
            }
        })
    }

    private fun createExpenseTransaction(amount: String, category: String, note: String) {
        val sharedPreferences = requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        // Create the request body
        val currentDate = LocalDate.now()
        val formattedDate = currentDate.format(DateTimeFormatter.ISO_DATE)
        val requestBody = JSONObject().apply {
            put("transactionDate", formattedDate)
            put("transactionAmount", amount.toDouble())
            put("transactionNote", note)
            put("category", JSONObject().apply {
                put("categoryId", (expenseCategories.indexOf(category)+1))
                put("categoryName", category)
            })
            put("type", JSONObject().apply {
                put("typeId", 2)
                put("typeName", "expense")
            })
            put("user", JSONObject().apply {
                put("userId", sharedPreferences.getString("user_id", null))
                put("username", sharedPreferences.getString("username", null))
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/transactions")
            .header("Authorization", "Bearer $authToken")
            .post(requestBody.toString().toRequestBody(mediaType))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                activity?.runOnUiThread {
                    // Update UI accordingly
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("TAG", "onResponse: $responseBody")
                if (response.isSuccessful && responseBody != null) {
                    // Success response, handle accordingly
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Transaction has been saved", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle unsuccessful response
                    activity?.runOnUiThread {
                        // Update UI accordingly
                    }
                }
            }
        })
    }

}