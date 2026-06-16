package com.launcher.aynthords.library

import android.os.Handler
import android.os.Looper
import com.launcher.aynthords.domain.repo.LibraryRepository

/**
 * Debounces package change events and triggers library refresh.
 */
object LibraryRefreshController {
    private const val DEBOUNCE_MS = 300L

    private val handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable? = null
    private var repository: LibraryRepository? = null

    fun setRepository(repo: LibraryRepository) {
        repository = repo
    }

    fun requestRefresh() {
        refreshRunnable?.let { handler.removeCallbacks(it) }
        refreshRunnable = Runnable {
            repository?.refresh()
            refreshRunnable = null
        }
        handler.postDelayed(refreshRunnable!!, DEBOUNCE_MS)
    }
}
