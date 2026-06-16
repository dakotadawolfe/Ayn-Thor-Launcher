package com.launcher.aynthords

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.launcher.aynthords.data.local.PackageIconFetcher

class ThorApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components { add(PackageIconFetcher.Factory()) }
            .build()
    }
}
