package com.launcher.aynthords

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.displayRoleDataStore by preferencesDataStore(name = "display_role_mapping")

class DisplayRoleMappingStore(context: Context) {
    private val appContext = context.applicationContext
    private val registry = DisplayRegistry(appContext)

    private object Keys {
        val interactionDisplayId = intPreferencesKey("interaction_display_id")
        val presentationDisplayId = intPreferencesKey("presentation_display_id")
    }

    fun resolveDisplayId(role: DisplayRole): Int? = runBlocking {
        val snapshot = registry.snapshot()
        val stored = appContext.displayRoleDataStore.data.first()[role.key()]
        val available = snapshot.allDisplayIds.toSet()

        if (stored != null && available.contains(stored)) {
            stored
        } else {
            val fallback = registry.resolveDefault(role)
            if (fallback != null) {
                appContext.displayRoleDataStore.edit { prefs: MutablePreferences ->
                    prefs[role.key()] = fallback
                }
            }
            fallback
        }
    }

    fun setDisplayId(role: DisplayRole, displayId: Int) = runBlocking {
        appContext.displayRoleDataStore.edit { prefs: MutablePreferences ->
            prefs[role.key()] = displayId
        }
    }

    private fun DisplayRole.key(): Preferences.Key<Int> = when (this) {
        DisplayRole.INTERACTION -> Keys.interactionDisplayId
        DisplayRole.PRESENTATION -> Keys.presentationDisplayId
    }
}
