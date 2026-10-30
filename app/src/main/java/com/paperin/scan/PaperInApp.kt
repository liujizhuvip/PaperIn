/*
 * Copyright 2025-2026 The FairScan authors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.paperin.scan

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.paperin.scan.data.FileLogger
import com.paperin.scan.data.FileManager
import com.paperin.scan.data.LogRepository
import com.paperin.scan.data.OcrLanguageRepository
import com.paperin.scan.domain.ImageSegmentationService
import com.paperin.scan.domain.OcrService
import com.paperin.scan.platform.AndroidImageLoader
import com.paperin.scan.platform.AndroidPdfWriter
import com.paperin.scan.ui.screens.camera.CameraViewModel
import com.paperin.scan.ui.screens.settings.SettingsRepository
import com.paperin.scan.ui.screens.settings.SettingsViewModel
import java.io.File

class PaperInApp : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        appContainer.cleanOrphanSessions()
    }
}

const val THUMBNAIL_SIZE_DP = 120

private val Context.dataStore by preferencesDataStore(name = "paperin_settings")

class AppContainer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cacheDir = context.cacheDir
    private val dataStore = context.dataStore
    val preparationDir = File(context.cacheDir, "pdfs")
    val ocrLanguageRepository =
        OcrLanguageRepository(
            dataStore,
            File(context.filesDir, "tesseract/tessdata"),
            context.assets,
        )
    val ocrService = OcrService(ocrLanguageRepository, scope)
    val fileManager = FileManager(
        preparationDir,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        AndroidPdfWriter(ocrService, context.assets)
    )
    val logRepository = LogRepository(File(context.filesDir, "logs.txt"))
    val logger = FileLogger(logRepository)
    val imageSegmentationService = ImageSegmentationService(context, logger)
    val imageLoader = AndroidImageLoader(context.contentResolver)
    val settingsRepository = SettingsRepository(context, dataStore, scope)

    init {
        // Extract the bundled Tesseract models before anything tries to read
        // them. Everything happens locally: the app has no network access.
        scope.launch { ocrLanguageRepository.provisionBundledLanguages() }
        scope.launch { imageSegmentationService.initialize() }
        scope.launch { ocrService.initialize() }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified VM : ViewModel> viewModelFactory(
        crossinline create: (AppContainer) -> VM
    ) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return create(this@AppContainer) as T
        }
    }

    val cameraViewModelFactory = viewModelFactory { CameraViewModel(it) }
    val settingsViewModelFactory = viewModelFactory { SettingsViewModel(it) }

    fun cleanOrphanSessions() {
        val sessionsRoot = sessionsRoot()
        if (!sessionsRoot.exists()) return

        val now = System.currentTimeMillis()

        sessionsRoot.listFiles()
            ?.filter { it.isDirectory }
            ?.forEach { dir ->
                if (isOldSession(dir, now)) {
                    dir.deleteRecursively()
                }
            }
    }

    fun sessionsRoot(): File = File(cacheDir, "sessions")

    private fun isOldSession(dir: File, now: Long): Boolean {
        val lastModified = dir.lastModified()
        return now - lastModified > 24 * 60 * 60 * 1000 // 24h
    }
}

class SessionViewModelFactory(
    private val application: Application,
    private val launchMode: LaunchMode,
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SessionViewModel(application, launchMode, appContainer) as T
    }
}
