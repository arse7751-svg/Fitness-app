package com.arsenii.fitnessapp

import android.content.Context
import android.content.SharedPreferences

object UserDataManager {

    private const val LOGIN_PREFS = "LoginPrefs"
    private const val ACTIVE_USER_KEY = "activeUser"

    private fun getLoginPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE)
    }

    fun getActiveUser(context: Context): String? {
        return getLoginPrefs(context).getString(ACTIVE_USER_KEY, null)
    }

    fun setActiveUser(context: Context, username: String) {
        getLoginPrefs(context).edit().putString(ACTIVE_USER_KEY, username).apply()
    }

    fun clearActiveUser(context: Context) {
        getLoginPrefs(context).edit().remove(ACTIVE_USER_KEY).apply()
    }

    // Generic function to get user-specific SharedPreferences
    fun getUserPrefs(context: Context, prefName: String): SharedPreferences {
        val currentUser = getActiveUser(context)
        val userPrefName = if (currentUser != null) "${prefName}_$currentUser" else prefName
        return context.getSharedPreferences(userPrefName, Context.MODE_PRIVATE)
    }
}
