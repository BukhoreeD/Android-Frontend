package com.example.personalfinancial

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HistoryFragment : Fragment() {
    private lateinit var transactionListView: ListView
    private lateinit var incomeButton: TextView
    private lateinit var expenseButton: TextView

    private val allTransactions: MutableList<Transaction> = mutableListOf()
    private val filteredTransactions: MutableList<Transaction> = mutableListOf()
    private lateinit var adapter: TransactionsAdapter

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        transactionListView = view.findViewById(R.id.transactionListView)
        incomeButton = view.findViewById(R.id.incomeButton)
        expenseButton = view.findViewById(R.id.expenseButton)

        // Set button click listeners
        incomeButton.setOnClickListener {
            showIncomeTransactions()
        }

        expenseButton.setOnClickListener {
            showExpenseTransactions()
        }

        // Fetch transaction history from API
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/transactions")
            .header("Authorization", "Bearer $authToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val transactionHistoryJson = it.string()
                    parseAndPopulateTransactionList(transactionHistoryJson)
                }
            }
        })

        // Initialize adapter
        adapter = TransactionsAdapter(requireContext(), filteredTransactions)

        // Set up the adapter and bind it to the ListView
        transactionListView.adapter = adapter


        // Set initial button state
        showIncomeTransactions()

        return view
    }

    private fun parseAndPopulateTransactionList(transactionHistoryJson: String) {
        try {
            val jsonArray = JSONArray(transactionHistoryJson)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val type = jsonObject.getJSONObject("type").getString("typeName")
                val amount = jsonObject.getDouble("transactionAmount")
                val note = jsonObject.getString("transactionNote")
                val category = jsonObject.getJSONObject("category").getString("categoryName")
                val date = jsonObject.getString("transactionDate")

                val transaction = Transaction(type, amount, note, category, date)
                allTransactions.add(transaction)
            }

            // Refresh the filtered transactions list
            refreshFilteredTransactions()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun refreshFilteredTransactions() {
        activity?.runOnUiThread {
            // Filter transactions based on the selected button
            if (incomeButton.isSelected) {
                showIncomeTransactions()
            } else {
                showExpenseTransactions()
            }
        }
    }

    private fun showIncomeTransactions() {
        // Filter transactions to show only income type
        filteredTransactions.clear()
        filteredTransactions.addAll(allTransactions.filter { it.type == "income" })

        // Update adapter
        adapter.notifyDataSetChanged()

        // Update button colors and text colors
        incomeButton.setBackgroundResource(R.drawable.income_rounded_background)
        expenseButton.setBackgroundResource(R.drawable.border_rounded_background)

        incomeButton.setTextColor(resources.getColor(android.R.color.white))
        expenseButton.setTextColor(resources.getColor(R.color.light_pink))
    }

    private fun showExpenseTransactions() {
        // Filter transactions to show only expense type
        filteredTransactions.clear()
        filteredTransactions.addAll(allTransactions.filter { it.type == "expense" })

        // Update adapter
        adapter.notifyDataSetChanged()

        incomeButton.setBackgroundResource(R.drawable.border_rounded_background)
        expenseButton.setBackgroundResource(R.drawable.expense_rounded_background)

        incomeButton.setTextColor(resources.getColor(R.color.light_green))
        expenseButton.setTextColor(resources.getColor(android.R.color.white))
    }

    private fun getAuthTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("authToken", null)
    }
}

class TransactionsAdapter(
    context: Context,
    transactions: List<Transaction>
) : ArrayAdapter<Transaction>(context, android.R.layout.simple_list_item_1, transactions) {

    override fun getItem(position: Int): Transaction? {
        return super.getItem(position)
    }

    override fun getCount(): Int {
        return super.getCount()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_transaction, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val transaction = getItem(position)

        // Set the data to the views in the custom layout
        viewHolder.typeTextView.text = transaction?.type
        viewHolder.amountTextView.text = "Amount: ${transaction?.amount}"
        viewHolder.noteTextView.text = "Note: ${transaction?.note}"
        viewHolder.categoryTextView.text = "Category: ${transaction?.category}"
        viewHolder.dateTextView.text = "Date: ${transaction?.date}"

        return view
    }

    private class ViewHolder(view: View) {
        val typeTextView: TextView = view.findViewById(R.id.typeTextView)
        val amountTextView: TextView = view.findViewById(R.id.amountTextView)
        val noteTextView: TextView = view.findViewById(R.id.noteTextView)
        val categoryTextView: TextView = view.findViewById(R.id.categoryTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
    }

}

data class Transaction(
    val type: String,
    val amount: Double,
    val note: String,
    val category: String,
    val date: String
) {
    override fun toString(): String {
        return "$type - $amount - $note - $category - $date"
    }
}

