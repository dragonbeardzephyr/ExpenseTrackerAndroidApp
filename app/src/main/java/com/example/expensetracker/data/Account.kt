package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val account_id:String,
    val available_balance:Double,
    val current_balance:Double,
    val currency_code:String,
    val name:String,
    val bankName:String,
    val type:String,
    val mask:String
)



