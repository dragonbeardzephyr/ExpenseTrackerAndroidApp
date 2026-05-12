package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NetWorth(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val amount: Double
)
