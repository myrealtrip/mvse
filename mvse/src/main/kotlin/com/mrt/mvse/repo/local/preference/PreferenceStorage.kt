package com.mrt.mvse.repo.local.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.mrt.mvse.repo.local.DEFAULT
import com.mrt.mvse.repo.local.Storage

class PreferenceStorage(val context: Context): Storage<String, String> {
    private fun getStorage(namespace: String?): SharedPreferences {
        return if (namespace.isNullOrEmpty() || namespace == DEFAULT) PreferenceManager.getDefaultSharedPreferences(context) else context.getSharedPreferences(namespace, Context.MODE_PRIVATE)
    }
    override fun put(namespace: String, key: String, value: String): Boolean {
        return getStorage(namespace).edit().putString(key, value).commit()
    }

    override fun get(namespace: String, key: String): String? {
        return getStorage(namespace).getString(key, null)
    }

    override fun contains(namespace: String, key: String): Boolean {
        return getStorage(namespace).contains(key)
    }

    override fun remove(namespace: String, key: String): Boolean {
        return getStorage(namespace).edit().remove(key).commit()
    }

    override fun wipe(namespace: String): Boolean {
        return getStorage(namespace).edit().clear().commit()
    }
}