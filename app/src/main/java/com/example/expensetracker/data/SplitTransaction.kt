package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "split_transactions",
    indices = [Index(value = ["parent_id"])],
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["transaction_id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["cat_id"],
            childColumns = ["cat_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)

data class SplitTransaction(
    @PrimaryKey(autoGenerate = true) val split_id: Int,
    val parent_id: String,
    val amount: Double,
    val name: String,
    val is_excluded: Boolean,
    val cat_id: Int? = null
)
