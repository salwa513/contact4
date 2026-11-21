package com.example.contacts2

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class ParametresFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
