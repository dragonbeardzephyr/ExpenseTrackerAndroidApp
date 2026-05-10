package com.example.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Account::class, Transaction::class, SplitTransaction::class], version = 1, exportSchema = false)

abstract class ExpensesDatabase: RoomDatabase() {
    abstract fun expensesDao(): ExpensesDao

    companion object {
        @Volatile
        private var Instance: ExpensesDatabase? = null
        fun getDatabase(context: Context):ExpensesDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ExpensesDatabase::class.java, "expenses")
                    .build().also { Instance = it }
            }
        }
    }


}