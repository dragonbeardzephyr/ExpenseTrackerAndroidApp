package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.expensetracker.ui.ExpensesApp

class MainActivity: ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                ExpensesApp()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }


}