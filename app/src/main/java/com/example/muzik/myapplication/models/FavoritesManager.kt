package com.example.muzik.myapplication.models

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun isFavorite(index: Int): Boolean = prefs.getBoolean("fav_$index", false)

    fun toggle(index: Int): Boolean {
        val current = isFavorite(index)
        prefs.edit().putBoolean("fav_$index", !current).apply()
        return !current
    }

    fun getAll(): Set<Int> {
        return prefs.all
            .filter { it.value == true }
            .mapNotNull { it.key.removePrefix("fav_").toIntOrNull() }
            .toSet()
    }
}
