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
            childColumns = ["cat_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ])

data class Budget(
    @PrimaryKey(autoGenerate = true) val budget_id:Int = 0,
    val budget_name: String,
    val cat_id: Int?,
    val limit: Double,
    val month: String // "YYYY-MM"
    )