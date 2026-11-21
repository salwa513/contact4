package com.example.contacts2

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

object PreferencesManager {
    const val KEY_TAILLE_POLICE = "taille_police"
    const val KEY_COULEUR_PREFEREE = "couleur_preferee"
    const val KEY_APPEL_LISTE = "appel_depuis_liste"

    fun getTaillePolice(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return (prefs.getString(KEY_TAILLE_POLICE, "16") ?: "16").toFloatOrNull() ?: 16f
    }

    fun getCouleurPreferee(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val colorStr = prefs.getString(KEY_COULEUR_PREFEREE, "#000000") ?: "#000000"
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            Color.BLACK
        }
    }

    fun isAppelDepuisListe(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(KEY_APPEL_LISTE, false)
    }
}
