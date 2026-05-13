package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NetWorth(
    @PrimaryKey(autoGenerate = true) val date: String, // YYYY-MM-DD, do an on replace thing here, if you sync more than once a day
    val amount: Double
)
