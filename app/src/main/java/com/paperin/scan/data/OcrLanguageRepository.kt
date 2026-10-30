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
 *
 * Modifications Copyright 2026 The PaperIn authors (see NOTICE).
 */
package com.paperin.scan.data

import android.content.res.AssetManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.paperin.scan.data.OcrLanguage.Companion.AVAILABLE_LANGUAGE_CODES
import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * Provides the Tesseract language data.
 *
 * PaperIn never downloads anything at runtime: the trained models bundled in
 * `assets/tessdata` are copied once into the app's private storage on first
 * launch. The app therefore works with no network connection at all, and does
 * not need the INTERNET permission.
 */
class OcrLanguageRepository(
    private val dataStore: DataStore<Preferences>,
    val tessdataDir: File,
    private val assets: AssetManager,
) {

    companion object {
        private const val ASSET_DIR = "tessdata"
        private const val EXTENSION = ".traineddata"

        private val ENABLED_LANGUAGES = stringSetPreferencesKey("enabled_ocr_languages")
        private val DEFAULTS_APPLIED = booleanPreferencesKey("ocr_language_defaults_applied")
    }

    private val provisionMutex = Mutex()

    @Volatile
    private var provisioned = false

    val enabledLanguages: Flow<Set<String>> =
        dataStore.data.map { prefs ->
            prefs[ENABLED_LANGUAGES] ?: emptySet()
        }

    /**
     * Copies the bundled traineddata files into [tessdataDir]. Idempotent and
     * cheap after the first call: existing files are skipped.
     */
    suspend fun provisionBundledLanguages() {
        if (provisioned) return
        provisionMutex.withLock {
            if (provisioned) return
            withContext(Dispatchers.IO) {
                if (!tessdataDir.exists() && !tessdataDir.mkdirs()) {
                    throw IOException("Cannot create $tessdataDir")
                }
                val bundled = assets.list(ASSET_DIR).orEmpty()
                    .filter { it.endsWith(EXTENSION) }
                    .filter { it.removeSuffix(EXTENSION) in AVAILABLE_LANGUAGE_CODES }

                for (name in bundled) {
                    val target = File(tessdataDir, name)
                    if (target.length() > 0) continue
                    val temp = File(tessdataDir, "$name.part")
                    try {
                        assets.open("$ASSET_DIR/$name").use { input ->
                            temp.outputStream().use { output -> input.copyTo(output) }
                        }
                        if (!temp.renameTo(target)) {
                            throw IOException("Cannot install $name")
                        }
                    } finally {
                        temp.delete()
                    }
                }
            }
            applyDefaultLanguages()
            provisioned = true
        }
    }

    /**
     * Enables the device language plus English the first time the app runs, so
     * text recognition works out of the box without any setup step.
     */
    private suspend fun applyDefaultLanguages() {
        if (dataStore.data.first()[DEFAULTS_APPLIED] == true) return
        val defaults = defaultLanguagesFor(Locale.getDefault())
            .intersect(AVAILABLE_LANGUAGE_CODES)
        dataStore.edit { prefs ->
            if (prefs[DEFAULTS_APPLIED] == true) return@edit
            if (prefs[ENABLED_LANGUAGES].isNullOrEmpty()) {
                prefs[ENABLED_LANGUAGES] = defaults
            }
            prefs[DEFAULTS_APPLIED] = true
        }
    }

    private fun defaultLanguagesFor(locale: Locale): Set<String> {
        val language = when (locale.language.lowercase(Locale.ROOT)) {
            "zh" -> when (locale.country.uppercase(Locale.ROOT)) {
                "TW", "HK", "MO" -> "chi_tra"
                else -> "chi_sim"
            }
            "en" -> "eng"
            "ja" -> "jpn"
            "ko" -> "kor"
            "ru" -> "rus"
            "de" -> "deu"
            "fr" -> "fra"
            "es" -> "spa"
            "vi" -> "vie"
            "th" -> "tha"
            else -> null
        }
        return setOfNotNull(language, "eng").toSet()
    }

    suspend fun getInstalledLanguages(): Set<String> {
        provisionBundledLanguages()
        return withContext(Dispatchers.IO) {
            tessdataDir
                .listFiles { _, name -> name.endsWith(EXTENSION) }
                ?.map { f -> f.nameWithoutExtension }
                ?.filter { it in AVAILABLE_LANGUAGE_CODES }
                ?.toSet()
                ?: emptySet()
        }
    }

    suspend fun getActiveLanguages(): Set<String> {
        val enabled = enabledLanguagesValue()
        val installed = getInstalledLanguages()
        return enabled.intersect(installed)
    }

    suspend fun setLanguageEnabled(
        code: String,
        enabled: Boolean,
    ) {
        require(code in AVAILABLE_LANGUAGE_CODES)
        if (enabled && code !in getInstalledLanguages()) {
            return
        }
        dataStore.edit { prefs ->
            val current = prefs[ENABLED_LANGUAGES]?.toMutableSet() ?: mutableSetOf()
            if (enabled) {
                current.add(code)
            } else {
                current.remove(code)
            }
            prefs[ENABLED_LANGUAGES] = current
        }
    }

    suspend fun buildTesseractLanguageString(): String {
        return getActiveLanguages()
            .sorted()
            .joinToString("+")
    }

    private suspend fun enabledLanguagesValue(): Set<String> {
        return enabledLanguages.map { it }.first()
    }
}
