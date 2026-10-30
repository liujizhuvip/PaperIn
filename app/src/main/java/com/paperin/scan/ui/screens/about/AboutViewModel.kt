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
package com.paperin.scan.ui.screens.about

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.paperin.scan.AppContainer
import com.paperin.scan.BuildConfig
import java.time.LocalDateTime

sealed interface AboutEvent {
    data class CopyLogs(val logs: String) : AboutEvent
}

class AboutViewModel(container: AppContainer): ViewModel() {

    private val logRepository = container.logRepository

    private val _events = MutableSharedFlow<AboutEvent>()
    val events = _events.asSharedFlow()

    fun onCopyLogsClicked() {
        viewModelScope.launch {
            val logs = buildFullLogs()
            _events.emit(AboutEvent.CopyLogs(logs))
        }
    }

    // Copied into the clipboard and then pasted into a GitHub issue: the app
    // deliberately has no mail client integration, so this is the only way to
    // get diagnostics out of the device.
    private fun buildFullLogs(): String {
        val header = buildString {
            appendLine("PaperIn diagnostics report")
            appendLine("App version: ${BuildConfig.VERSION_NAME}")
            appendLine("Android version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Generated: ${LocalDateTime.now()}")
            appendLine()
            appendLine("-- Application logs --")
            appendLine()
        }
        return header + logRepository.getLogs()
    }
}
