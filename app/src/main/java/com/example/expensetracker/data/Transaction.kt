package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["account_id"])],
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["account_id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Transaction(
    @PrimaryKey val transaction_id:String,
    val account_id:String,
    val amount:Double,
    val transaction_code:String,
    val date: LocalDateTime,
    val merchant_name:String,
    val name:String,
    val is_excluded:Boolean,
    val cat_primary:String?,
    val cat_detailed:String?,
    val pending:Boolean,
    val is_split:Boolean = false
)


