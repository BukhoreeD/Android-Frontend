package com.example.personalfinancial

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class SettingFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        sharedPreferences = requireContext().getSharedPreferences("Myprefs", Context.MODE_PRIVATE)

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        val forgotPasswordButton = view.findViewById<Button>(R.id.forgotPasswordButton)
        forgotPasswordButton.setOnClickListener {
            val intent = Intent(requireContext(), ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun showLogoutConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                deleteAuthToken()
                navigateToLoginPage()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
//                Toast.makeText(requireContext(), "user_id: "+sharedPreferences.getString("user_id", null), Toast.LENGTH_LONG).show()
            })
            .create()
            .show()
    }

    private fun deleteAuthToken() {
        val editor = sharedPreferences.edit()
        editor.remove("authToken")
        editor.apply()
    }

    private fun navigateToLoginPage() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
