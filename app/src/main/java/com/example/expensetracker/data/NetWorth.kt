package com.example.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "net_worth")
data class NetWorth(
    @PrimaryKey() val date: LocalDate, // YYYY-MM-DD, do an on replace thing here, if you sync more than once a day
    val amount: Double
)
