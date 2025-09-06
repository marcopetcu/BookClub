package com.example.bookclub.data

import android.content.Context
import com.example.bookclub.data.db.AppDatabase

object ServiceLocator {
    fun db(context: Context) = AppDatabase.get(context)
}
