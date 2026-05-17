package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "budgets",
    indices = [Index(value = ["cat_id"])],
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["cat_id"],
            childColumns = ["cat_id"]
        )
    ])

data class Budget(
    @PrimaryKey val budget_id:Int,
    val budget_name: String,
    val cat_id: Int?,
    val limit: Double,
    val month: String // "YYYY-MM"
    )