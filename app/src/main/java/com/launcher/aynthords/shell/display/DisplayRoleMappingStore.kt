package com.launcher.aynthords.shell.display

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.launcher.aynthords.domain.model.SurfaceRole
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

    fun resolveDisplayId(role: SurfaceRole): Int? = runBlocking {
        val snapshot = registry.snapshot()
        val stored = appContext.displayRoleDataStore.data.first()[role.key()]
        val available = snapshot.allDisplayIds.toSet()

        if (stored != null && available.contains<Int>(stored)) {
            stored
        } else {
            val fallback = registry.resolveDefault(role)
            if (fallback != null) {
                appContext.displayRoleDataStore.edit { prefs: MutablePreferences ->
                    prefs[role.key()] = fallback as Int
                }
            }
            fallback
        }
    }

    fun setDisplayId(role: SurfaceRole, displayId: Int) = runBlocking {
        appContext.displayRoleDataStore.edit { prefs: MutablePreferences ->
            prefs[role.key()] = displayId
        }
    }

    private fun SurfaceRole.key(): Preferences.Key<Int> = when (this) {
        SurfaceRole.INTERACTION -> Keys.interactionDisplayId
        SurfaceRole.PRESENTATION -> Keys.presentationDisplayId
    }
}
