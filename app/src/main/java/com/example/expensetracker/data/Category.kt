package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")

data class Category(

    @PrimaryKey(autoGenerate = true) val cat_id:Int = 0,
    val cat_name:String,
    val cat_type: String,
    val cat_plaid: String,
    val is_excluded: Boolean = false,

)