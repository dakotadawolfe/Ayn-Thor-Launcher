package com.launcher.aynthords

import android.content.Context
import org.json.JSONObject
import java.io.File

/** Writes NDJSON to app filesDir for adb pull / run-as. Path: files/debug_agent.ndjson */
object DebugLog {
    private fun file(context: Context): File = File(context.filesDir, "debug_agent.ndjson")

    fun log(context: Context, hypothesisId: String, location: String, message: String, vararg data: Pair<String, Any?>) {
        val obj = JSONObject()
        data.forEach { (k, v) -> obj.put(k, v?.toString() ?: "null") }
        val line = JSONObject().apply {
            put("hypothesisId", hypothesisId)
            put("location", location)
            put("message", message)
            put("data", obj)
            put("timestamp", System.currentTimeMillis())
        }.toString() + "\n"
        try {
            file(context.applicationContext).appendText(line)
        } catch (_: Exception) {}
    }
}
