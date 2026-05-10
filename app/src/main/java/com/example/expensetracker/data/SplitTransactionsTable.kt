package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "split_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["transaction_id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SplitTransaction(
    @PrimaryKey(autoGenerate = true) val split_id: Int,
    val parent_id: String,
    val amount: Double,
    val name: String,
    val is_exlcuded: Boolean,
    val cat_primary: String?,
    val cat_detailed: String?
)
