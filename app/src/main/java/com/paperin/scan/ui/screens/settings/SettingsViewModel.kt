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
package com.paperin.scan.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.paperin.scan.AppContainer
import com.paperin.scan.domain.ExportQuality

data class SettingsUiState(
    val defaultColorMode: DefaultColorMode = DefaultColorMode.AUTO,
    val export: ExportSettingsUiState = ExportSettingsUiState(),
    val installedOcrLanguages: Set<String> = emptySet(),
    val enabledOcrLanguages: Set<String> = emptySet(),
)

data class ExportSettingsUiState(
    val dirUri: String? = null,
    val dirName: String? = null,
    val format: ExportFormat = ExportFormat.PDF,
    val quality: ExportQuality = ExportQuality.BALANCED,
    val filenameStyle: DefaultFilenameStyle = DefaultFilenameStyle.SCAN_DATE_TIME,
)

class SettingsViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.settingsRepository
    private val ocrLanguageRepo = container.ocrLanguageRepository

    private val _installedLanguages = MutableStateFlow<Set<String>>(emptySet())

    private val _dirName = MutableStateFlow<String?>(null)
    val dirName: StateFlow<String?> = _dirName

    private val exportSettingsState =
        combine(
            repo.exportDirUri,
            dirName,
            repo.exportFormat,
            repo.exportQuality,
            repo.defaultFilenameStyle,
        ) {
            dirUri, dirName, format, quality, filenameStyle ->
            ExportSettingsUiState(dirUri, dirName, format, quality, filenameStyle)
        }
    val uiState = combine(
        repo.defaultColorMode,
        exportSettingsState,
        _installedLanguages,
        ocrLanguageRepo.enabledLanguages,
    ) { colorMode, exportSettings, installed, enabled ->
        SettingsUiState(
            defaultColorMode = colorMode,
            export = exportSettings,
            installedOcrLanguages = installed,
            enabledOcrLanguages = enabled,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsUiState()
    )

    private suspend fun refreshInstalledLanguages() {
        _installedLanguages.value =
            ocrLanguageRepo.getInstalledLanguages()
    }

    init {
        viewModelScope.launch {
            refreshInstalledLanguages()
        }
    }

    fun setDefaultColorMode(pref: DefaultColorMode) {
        viewModelScope.launch {
            repo.setDefaultColorMode(pref)
        }
    }

    fun setExportDirUri(uri: String?) {
        viewModelScope.launch {
            repo.setExportDirUri(uri)
            refreshExportDirName()
        }
    }

    fun setExportFormat(format: ExportFormat) {
        viewModelScope.launch {
            repo.setExportFormat(format)
        }
    }

    fun setExportQuality(quality: ExportQuality) {
        viewModelScope.launch {
            repo.setExportQuality(quality)
        }
    }

    fun setDefaultFilenameStyle(style: DefaultFilenameStyle) {
        viewModelScope.launch {
            repo.setDefaultFilenameStyle(style)
        }
    }

    fun refreshExportDirName() {
        viewModelScope.launch {
            val uri = repo.exportDirUri.first()
            _dirName.value = uri?.let { repo.resolveExportDirName(it) }
        }
    }

    /** Every bundled language is already on disk, so a tap simply toggles it. */
    fun onLanguageClick(code: String) {
        setOcrLanguageEnabled(code, code !in uiState.value.enabledOcrLanguages)
    }

    fun setOcrLanguageEnabled(code: String, enabled: Boolean) {
        viewModelScope.launch {
            ocrLanguageRepo.setLanguageEnabled(code, enabled)
        }
    }
}
