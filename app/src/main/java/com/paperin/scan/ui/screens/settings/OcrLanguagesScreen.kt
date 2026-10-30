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
package com.paperin.scan.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paperin.scan.R
import com.paperin.scan.data.OcrLanguage
import com.paperin.scan.ui.components.BackButton
import com.paperin.scan.ui.theme.PaperInTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrLanguagesScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onLanguageClick: (String) -> Unit,
) {
    BackHandler { onBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_ocr_languages),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = { BackButton(onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
    ) { paddingValues ->
        OcrLanguagesContent(
            uiState = uiState,
            onLanguageClick = onLanguageClick,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun OcrLanguagesContent(
    uiState: SettingsUiState,
    onLanguageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.current.platformLocale

    val languages = remember(
        uiState.installedOcrLanguages,
        locale,
    ) {
        uiState.installedOcrLanguages
            .map { OcrLanguage(it) }
            .sortedWith(compareBy { it.displayName(locale) })
    }

    val enabledCount = uiState.enabledOcrLanguages.count { it in uiState.installedOcrLanguages }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_ocr_offline_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.settings_ocr_offline_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }

        items(
            items = languages,
            key = { it.code }
        ) { lang ->
            val checked = lang.code in uiState.enabledOcrLanguages
            // Turning the last language off would silently disable text
            // recognition, so keep one enabled at all times.
            val switchEnabled = !checked || enabledCount > 1

            LanguageItem(
                language = lang,
                checked = checked,
                switchEnabled = switchEnabled,
                onClick = { if (switchEnabled) onLanguageClick(lang.code) },
            )
        }

        if (enabledCount <= 1) {
            item {
                Text(
                    text = stringResource(R.string.settings_ocr_keep_one),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: OcrLanguage,
    checked: Boolean,
    switchEnabled: Boolean,
    onClick: () -> Unit,
) {
    val locale = Locale.current.platformLocale

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = switchEnabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = language.displayName(locale),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = language.code,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = { onClick() },
                enabled = switchEnabled,
            )
        }
    }
}

@Preview
@Composable
fun OcrLanguagesScreenPreviewBasic() {
    PaperInTheme {
        OcrLanguagesScreen(
            SettingsUiState(
                installedOcrLanguages = setOf("chi_sim", "eng", "jpn", "deu"),
                enabledOcrLanguages = setOf("chi_sim", "eng"),
            ),
            {},
            {},
        )
    }
}
