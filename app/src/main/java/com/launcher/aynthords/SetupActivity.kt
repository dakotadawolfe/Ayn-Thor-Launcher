package com.launcher.aynthords

import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.shell.display.DisplayRegistry
import com.launcher.aynthords.shell.display.DisplayRoleMappingStore
import com.launcher.aynthords.shell.display.ensureSecondaryDisplayActivity

class SetupActivity : AppCompatActivity() {

    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple UI built in code so you don't have to mess with XML yet
        val root = ScrollView(this)
        val container = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val btnEnable = Button(this).apply {
            text = "Enable Dual-Screen Home"
        }

        val btnRestore = Button(this).apply {
            text = "Restore Defaults"
        }

        logView = TextView(this).apply {
            text = "Ready.\n"
            setPadding(0, 24, 0, 0)
        }

        container.addView(btnEnable)
        container.addView(btnRestore)
        container.addView(logView)
        root.addView(container)

        setContentView(root)

        btnEnable.setOnClickListener { enableDualScreenHome() }
        btnRestore.setOnClickListener { restoreDefaults() }
    }

    private fun enableDualScreenHome() {
        log("Resolving presentation display...\n")
        launchPresentationOnSecondaryDisplay()
    }

    private fun restoreDefaults() {
        log("Restoring defaults (re-enabling known secondary home components)...\n")

        // We re-enable everything we previously disabled.
        // This uses the same candidate list and enables any not in keepPackage.
        val candidates = SecondaryHomeUtil.querySecondaryHomeCandidates(this)
        val commands = SecondaryHomeUtil.buildEnableCommands(
            keepPackage = "com.launcher.aynthords",
            candidates = candidates
        )

        val (code, output) = RootShell.run(*commands.toTypedArray())
        log("Root exitCode=$code\n")
        if (output.isNotBlank()) log(output + "\n")

        if (code != 0) {
            log("\n❌ Restore failed.\n")
            return
        }

        log("\n✅ Restored. You may need to press Home again on the bottom screen.\n")
    }

    private fun launchPresentationOnSecondaryDisplay() {
        val mappingStore = DisplayRoleMappingStore(this)
        val registry = DisplayRegistry(applicationContext)
        val snapshot = registry.snapshot()
        val presentationDisplayId = mappingStore.resolveDisplayId(SurfaceRole.PRESENTATION)
            ?: snapshot.presentationCandidates.firstOrNull()
            ?: getLikelySecondaryDisplayId()

        if (presentationDisplayId == null) {
            log("❌ Could not find a secondary display.\n")
            return
        }

        mappingStore.setDisplayId(SurfaceRole.PRESENTATION, presentationDisplayId)
        log("Presentation displayId=$presentationDisplayId\n")

        try {
            ensureSecondaryDisplayActivity(presentationDisplayId)
            log("✅ Started presentation host on display $presentationDisplayId.\n")
        } catch (t: Throwable) {
            log("❌ Failed to start presentation host: ${t.message}\n")
        }
    }

    private fun getLikelySecondaryDisplayId(): Int? {
        val dm = getSystemService(DisplayManager::class.java)
        val displays = dm.displays

        // Prefer any non-default display
        val secondary = displays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }
        return secondary?.displayId
    }

    private fun log(s: String) {
        logView.append(s)
    }
}
