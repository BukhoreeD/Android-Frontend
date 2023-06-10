package com.example.personalfinancial

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Calendar

class PlanningFragment : Fragment() {
    private lateinit var incomeGoalEditText: EditText
    private lateinit var expenseLimitEditText: EditText

    private lateinit var incomeGoalButton: Button
    private lateinit var expenseLimitButton: Button

    private lateinit var monthYearTextView: TextView
    private lateinit var remainingIncomeGoalTextViewNumber: TextView
    private lateinit var remainingExpenseLimitTextViewNumber: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_planning, container, false)

        incomeGoalEditText = view.findViewById(R.id.incomeGoalEditText)
        expenseLimitEditText = view.findViewById(R.id.expenseLimitEditText)

        incomeGoalButton = view.findViewById(R.id.incomeGoalButton)
        expenseLimitButton = view.findViewById(R.id.expenseLimitButton)

        monthYearTextView = view.findViewById(R.id.monthYearTextView)
        remainingIncomeGoalTextViewNumber = view.findViewById(R.id.reamaingIncomeGoalTextViewNumber)
        remainingExpenseLimitTextViewNumber = view.findViewById(R.id.reamaingExpenseLimitTextViewNumber)

        incomeGoalButton.setOnClickListener {
            setIncomeGoal()
            refreshFragment()
        }

        expenseLimitButton.setOnClickListener {
            setExpenseLimit()
            refreshFragment()
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        val monthNames = arrayOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        val currentMonth = monthNames[calendar.get(Calendar.MONTH)]
        val planningTextTitle = "Planning for $currentMonth $currentYear"
        monthYearTextView.text = planningTextTitle

        // Enable EditText and Button for Income Goal
        incomeGoalEnabled(true)

        fetchIncomeGoal { incomeGoal ->
            activity?.runOnUiThread {
                if (incomeGoal != 0.0) {
                    incomeGoalEnabled(false)
                    incomeGoalButton.setBackgroundResource(R.drawable.border_rounded_background)

                    // Set income goal value in EditText
                    incomeGoalEditText.setText(incomeGoal.toString())
                    fetchTotalIncome { totalIncome ->
                        activity?.runOnUiThread {
                            if (totalIncome != 0.0) {
                                val remainingIncomeGoal = incomeGoal - totalIncome
                                remainingIncomeGoalTextViewNumber.text = remainingIncomeGoal.toString()
                            } else {
                                remainingIncomeGoalTextViewNumber.text = "0"
                            }
                        }
                    }
                } else {
                    // Enable EditText and Button to set income goal
                    incomeGoalEnabled(true)
                }
            }
        }

        // Enable EditText and Button for Expense Limit
        expenseLimitEnabled(true)

        fetchExpenseLimit { expenseLimit ->
            activity?.runOnUiThread {
                if (expenseLimit != 0.0) {
                    expenseLimitEnabled(false)
                    expenseLimitButton.setBackgroundResource(R.drawable.border_rounded_background)

                    // Set income goal value in EditText
                    expenseLimitEditText.setText(expenseLimit.toString())
                    fetchTotalExpense { totalExpense ->
                        activity?.runOnUiThread {
//                            Toast.makeText(requireContext(), "Total: $totalExpense, Expense Limit: $expenseLimit", Toast.LENGTH_SHORT).show()
                            if (totalExpense != 0.0) {
                                val remainingIncomeGoal = expenseLimit - totalExpense
                                remainingExpenseLimitTextViewNumber.text = remainingIncomeGoal.toString()
                            } else {
                                remainingExpenseLimitTextViewNumber.text = "0"
                            }
                        }
                    }
                } else {
                    // Enable EditText and Button to set income goal
                    expenseLimitEnabled(true)
                }
            }
        }

        return view
    }

    private fun incomeGoalEnabled(enabled: Boolean) {
        incomeGoalEditText.isEnabled = enabled
        incomeGoalButton.isEnabled = enabled

    }

    private fun expenseLimitEnabled(enabled: Boolean) {
        expenseLimitEditText.isEnabled = enabled
        expenseLimitButton.isEnabled = enabled
    }

    private fun setIncomeGoal() {
        val client = OkHttpClient()

        val incomeGoalText = incomeGoalEditText.text.toString()
        val incomeTarget = incomeGoalText.toDoubleOrNull()
        val currentDate = LocalDate.now()
        val formattedDate = currentDate.format(DateTimeFormatter.ISO_DATE)

        val sharedPreferences = requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        if (incomeTarget != null) {
            val incomeRequestBody = JSONObject().apply {
                put("incomeTarget", incomeTarget)
                put("monthYear", formattedDate)
                put("user", JSONObject().apply {
                    put("userId", sharedPreferences.getString("user_id", null))
                    put("username", sharedPreferences.getString("username", null))
                })
            }

            val authToken = getAuthTokenFromSharedPreferences()
            val incomeMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val incomeRequest = Request.Builder()
                .url("http://10.0.2.2:8080/budgets/income")
                .header("Authorization", "Bearer $authToken")
                .post(incomeRequestBody.toString().toRequestBody(incomeMediaType))
                .build()

            client.newCall(incomeRequest).enqueue(object : Callback {
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
                        val responseIncomeJson = JSONObject(responseBody)
                        val responseIncomeTarget = responseIncomeJson.getDouble("incomeTarget")

                        activity?.runOnUiThread {
                            // Update UI with income goal data
                            incomeGoalEditText.setText(responseIncomeTarget.toString())
                        }
                    } else {
                        // Handle unsuccessful response
                        activity?.runOnUiThread {
                            // Update UI accordingly
                        }
                    }
                }
            })
        } else {
            Toast.makeText(requireContext(), "Please enter income target", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setExpenseLimit() {
        val client = OkHttpClient()

        val expenseGoalText = expenseLimitEditText.text.toString()
        val expenseTarget = expenseGoalText.toDoubleOrNull()
        val currentDate = LocalDate.now()
        val formattedDate = currentDate.format(DateTimeFormatter.ISO_DATE)

        val sharedPreferences = requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        if (expenseTarget != null) {
            val expenseRequestBody = JSONObject().apply {
                put("expenseTarget", expenseTarget)
                put("monthYear", formattedDate)
                put("user", JSONObject().apply {
                    put("userId", sharedPreferences.getString("user_id", null))
                    put("username", sharedPreferences.getString("username", null))
                })
            }

            val authToken = getAuthTokenFromSharedPreferences()
            val expenseMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val incomeRequest = Request.Builder()
                .url("http://10.0.2.2:8080/budgets/expense")
                .header("Authorization", "Bearer $authToken")
                .post(expenseRequestBody.toString().toRequestBody(expenseMediaType))
                .build()

            client.newCall(incomeRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure
                    activity?.runOnUiThread {
                        // Update UI accordingly
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        val responseExpenseJson = JSONObject(responseBody)
                        val responseExpenseTarget = responseExpenseJson.getDouble("expenseTarget")

                        activity?.runOnUiThread {
                            // Update UI with income goal data
                            expenseLimitEditText.setText(responseExpenseTarget.toString())
                        }
                    } else {
                        // Handle unsuccessful response
                        activity?.runOnUiThread {
                            // Update UI accordingly
                        }
                    }
                }
            })
        } else {
            Toast.makeText(requireContext(), "Please enter income target", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun fetchIncomeGoal(callback: (Double) -> Unit) {
        val client = OkHttpClient()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Months are zero-based, so add 1
        val currentYear = calendar.get(Calendar.YEAR)
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/budgets/income?month=$currentMonth&year=$currentYear")
            .header("Authorization", "Bearer $authToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(0.0) // Call the callback with a default value in case of failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val incomeGoalString = it.string()
                    val incomeGoal = incomeGoalString.toDoubleOrNull() ?: 0.0
                    callback(incomeGoal)
                }
            }
        })
    }

    private fun fetchExpenseLimit(callback: (Double) -> Unit) {
        val client = OkHttpClient()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Months are zero-based, so add 1
        val currentYear = calendar.get(Calendar.YEAR)
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/budgets/expense?month=$currentMonth&year=$currentYear")
            .header("Authorization", "Bearer $authToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(0.0) // Call the callback with a default value in case of failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val expenseLimitString = it.string()
                    val expenseLimit = expenseLimitString.toDoubleOrNull() ?: 0.0
                    callback(expenseLimit)
                }
            }
        })
    }

    // Get total income of current month
    private fun fetchTotalIncome(callback: (Double) -> Unit) {
        val client = OkHttpClient()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Months are zero-based, so add 1
        val currentYear = calendar.get(Calendar.YEAR)
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/transactions/income/total?month=$currentMonth&year=$currentYear")
            .header("Authorization", "Bearer $authToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(0.0) // Call the callback with a default value in case of failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val totalIncomeString = it.string()
                    val totalIncome = totalIncomeString.toDoubleOrNull() ?: 0.0
                    callback(totalIncome)
                }
            }
        })
    }

    // Get total expense of current month
    private fun fetchTotalExpense(callback: (Double) -> Unit) {
        val client = OkHttpClient()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Months are zero-based, so add 1
        val currentYear = calendar.get(Calendar.YEAR)
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/transactions/expense/total?month=$currentMonth&year=$currentYear")
            .header("Authorization", "Bearer $authToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(0.0) // Call the callback with a default value in case of failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val totalExpenseString = it.string()
                    val totalExpense = totalExpenseString.toDoubleOrNull() ?: 0.0
                    callback(totalExpense)
                }
            }
        })
    }

    private fun getAuthTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("authToken", null)
    }

    private fun refreshFragment() {
        val currentFragment = requireParentFragment()
        val transaction = parentFragmentManager.beginTransaction()

        // Replace the existing fragment with a new instance of the same fragment
        transaction.replace(currentFragment.id, PlanningFragment())

        // Add the transaction to the back stack (optional)
        transaction.addToBackStack(null)

        // Commit the transaction
        transaction.commit()
    }

}
