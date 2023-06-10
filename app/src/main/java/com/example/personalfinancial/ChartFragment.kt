package com.example.personalfinancial

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Calendar

class ChartFragment : Fragment() {
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)
        barChart = view.findViewById(R.id.barChart)
        lineChart = view.findViewById(R.id.lineChart)

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Months are zero-based, so add 1
        val currentYear = calendar.get(Calendar.YEAR)
        var year = currentYear
        var startMonthIndex = currentMonth

        if (currentMonth > 2) {
            startMonthIndex = currentMonth - 2
        } else {
            startMonthIndex = currentMonth + 9
            year -= 1
        }

        // Fetch the total income and expense values asynchronously
        fetchTotalIncomeOrExpense("income", startMonthIndex, year) { firstMonthIncome ->
            fetchTotalIncomeOrExpense("income", (startMonthIndex + 1) % 12, year) { secondMonthIncome ->
                fetchTotalIncomeOrExpense("income", currentMonth, currentYear) { thirdMonthIncome ->
                    fetchTotalIncomeOrExpense("expense", startMonthIndex, year) { firstMonthExpense ->
                        fetchTotalIncomeOrExpense("expense", (startMonthIndex + 1) % 12, year) { secondMonthExpense ->
                            fetchTotalIncomeOrExpense("expense", currentMonth, currentYear) { thirdMonthExpense ->
                                val incomeData = floatArrayOf(firstMonthIncome, secondMonthIncome, thirdMonthIncome) // Income for each month
                                val expenseData = floatArrayOf(firstMonthExpense, secondMonthExpense, thirdMonthExpense) // Expense for each month

                                // Set up the bar and line charts with the obtained data
                                setupBarChart(startMonthIndex, incomeData, expenseData)
                                setupLineChart(startMonthIndex, incomeData, expenseData)
                            }
                        }
                    }
                }
            }
        }

        return view
    }

    private fun fetchTotalIncomeOrExpense(type: String, month: Int, year: Int, callback: (Float) -> Unit) {
        val client = OkHttpClient()
        val authToken = getAuthTokenFromSharedPreferences()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/transactions/$type/total?month=$month&year=$year")
            .header("Authorization", "Bearer $authToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the failure case here
                callback(0.0f)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let {
                        val totalString = it.string()
                        val total = totalString.toFloatOrNull() ?: 0.0f
                        callback(total)
                    }
                } else {
                    response.close()
                    callback(0.0f)
                }
            }
        })
    }


    private fun setupBarChart(startMonthIndex: Int, incomeData: FloatArray, expenseData: FloatArray) {
        // Create sample data for the bar chart (replace with your own data)
//        val incomeData = floatArrayOf(1000f, 1500f, 1200f) // Income for each month
//        val expenseData = floatArrayOf(800f, 1200f, 900f) // Expense for each month

        // Create entries for the bar chart
        val entries = mutableListOf<BarEntry>()
        for (i in incomeData.indices) {
            entries.add(BarEntry(i.toFloat(), floatArrayOf(incomeData[i], expenseData[i])))
        }

        // Create a BarDataSet from the entries
        val dataSet = BarDataSet(entries, "Data Set")
        dataSet.colors = listOf(Color.parseColor("#9EC690"), Color.parseColor("#FF6584")) // Set different colors for income and expense bars
        dataSet.stackLabels = arrayOf("Income", "Expense") // Labels for the stacked bars

        // Create a BarData object and set it to the BarChart
        val barData = BarData(dataSet)
        barChart.data = barData

        // Customize the BarChart appearance
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.xAxis.labelCount = incomeData.size
        barChart.xAxis.valueFormatter = MonthValueFormatter(startMonthIndex) // Custom X-axis value formatter
        barChart.axisRight.isEnabled = false
        barChart.setScaleEnabled(false) // Disable zooming in/out

        // Refresh the chart
        barChart.invalidate()
    }

    private fun setupLineChart(startMonthIndex: Int, incomeData: FloatArray, expenseData: FloatArray) {

        val lineEntriesIncome = mutableListOf<Entry>()
        val lineEntriesExpense = mutableListOf<Entry>()

        for (i in incomeData.indices) {
            lineEntriesIncome.add(Entry(i.toFloat(), incomeData[i]))
            lineEntriesExpense.add(Entry(i.toFloat(), expenseData[i]))
        }

        val lineDataSetIncome = LineDataSet(lineEntriesIncome, "Income")
        lineDataSetIncome.color = Color.BLUE
        lineDataSetIncome.setCircleColor(Color.BLUE)
        lineDataSetIncome.setDrawValues(false) // Disable value text display on data points

        val lineDataSetExpense = LineDataSet(lineEntriesExpense, "Expense")
        lineDataSetExpense.color = Color.RED
        lineDataSetExpense.setCircleColor(Color.RED)
        lineDataSetExpense.setDrawValues(false) // Disable value text display on data points

        val lineData = LineData(lineDataSetIncome, lineDataSetExpense)

        lineChart.data = lineData

        // Customize the LineChart appearance
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.xAxis.labelCount = incomeData.size
        lineChart.xAxis.valueFormatter = MonthValueFormatter(startMonthIndex) // Custom X-axis value formatter
        lineChart.axisRight.isEnabled = false
        lineChart.setScaleEnabled(false) // Disable zooming in/out

        // Refresh the chart
        lineChart.invalidate()
    }

    private fun getAuthTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("authToken", null)
    }
}

class MonthValueFormatter(private val startMonthIndex: Int) : ValueFormatter() {

    private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = (value.toInt() + (startMonthIndex - 1)) % months.size
        return months[index]
    }
}



