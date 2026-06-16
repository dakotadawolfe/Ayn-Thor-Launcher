package com.launcher.aynthords.data.local

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer
import java.io.ByteArrayOutputStream

private const val PKG_SCHEME = "pkg:"

/**
 * Coil Fetcher for app icons via package name.
 * Data format: "pkg:com.example.app"
 */
class PackageIconFetcher(
    private val data: String,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        if (!data.startsWith(PKG_SCHEME)) {
            throw IllegalArgumentException("PackageIconFetcher expects data starting with $PKG_SCHEME, got: $data")
        }
        val packageName = data.removePrefix(PKG_SCHEME)
        val pm = options.context.packageManager
        val drawable = try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            return SourceResult(
                source = emptyImageSource(options.context),
                mimeType = null,
                dataSource = DataSource.MEMORY,
            )
        }
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
            ?: drawable.toBitmap()
        val bytes = ByteArrayOutputStream().use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
        val buffer = Buffer().write(bytes)
        return SourceResult(
            source = ImageSource(buffer, options.context),
            mimeType = "image/png",
            dataSource = DataSource.MEMORY,
        )
    }

    private fun Drawable.toBitmap(): android.graphics.Bitmap {
        val w = intrinsicWidth.coerceAtLeast(1)
        val h = intrinsicHeight.coerceAtLeast(1)
        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    private fun emptyImageSource(context: android.content.Context): ImageSource {
        val empty = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
        val bytes = ByteArrayOutputStream().use { stream ->
            empty.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
        return ImageSource(Buffer().write(bytes), context)
    }

    class Factory : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            return if (data.startsWith(PKG_SCHEME)) PackageIconFetcher(data, options) else null
        }
    }
}
