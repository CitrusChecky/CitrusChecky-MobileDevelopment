package com.rivaphys.citruschecky.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)


    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true)
    }

    fun setFirstTime(isFirstTime: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_FIRST_TIME, isFirstTime)
            .apply()
    }

    companion object {
        private const val KEY_FIRST_TIME = "is_first_time"
    }
}