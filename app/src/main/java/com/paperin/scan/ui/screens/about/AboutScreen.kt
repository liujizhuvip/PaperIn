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

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.paperin.scan.BuildConfig
import com.paperin.scan.R
import com.paperin.scan.ui.components.BackButton
import com.paperin.scan.ui.theme.Brand
import com.paperin.scan.ui.theme.PaperInTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    aboutUiState: AboutUiState,
    onBack: () -> Unit,
    onCopyLogs: () -> Unit,
    onContactWithLastImageClicked: () -> Unit,
    onStartActivity: (Intent) -> Unit,
    onViewLibraries: () -> Unit,
) {
    val showLicenseDialog = rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    BackHandler { onBack() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.about),
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
        AboutContent(
            modifier = Modifier.padding(paddingValues),
            aboutUiState,
            onCopyLogs,
            onContactWithLastImageClicked,
            onStartActivity,
            showLicenseDialog,
            onViewLibraries)
    }
    if (showLicenseDialog.value) {
        LicenseBottomSheet(sheetState, onDismiss = { showLicenseDialog.value = false })
    }
}

@Composable
fun AboutContent(
    modifier: Modifier = Modifier,
    aboutUiState: AboutUiState,
    onCopyLogs: () -> Unit,
    onContactWithLastImageClicked: () -> Unit,
    onStartActivity: (Intent) -> Unit,
    showLicenseDialog: MutableState<Boolean>,
    onViewLibraries: () -> Unit,
    ) {

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Brand.GradientStart, Brand.GradientEnd)
                    )
                )
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.app_tagline),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
            }
        }

        Section(title = stringResource(R.string.version)) {
            Text(BuildConfig.VERSION_NAME)
        }

        Section(title = stringResource(R.string.developer)) {
            Text("PaperIn Studio")
        }

        Section(title = stringResource(R.string.contact)) {
            ContactLink(
                icon = Icons.Default.Email,
                text = EMAIL_ADDRESS,
                onClick = { onStartActivity(createContactEmailIntent()) }
            )
        }

        Section(title = stringResource(R.string.support)) {
            EmailImageButton(aboutUiState, onContactWithLastImageClicked)
            CopyLogsButton (onClick = onCopyLogs)
        }

        Section(title = stringResource(R.string.license)) {
            Text(
                stringResource(R.string.licensed_under),
            )
            Text(
                text = stringResource(R.string.view_the_full_license),
                modifier = Modifier.clickable { showLicenseDialog.value = true },
                color = MaterialTheme.colorScheme.primary
            )
        }

        Section(title = stringResource(R.string.about_upstream_title)) {
            Text(
                stringResource(R.string.about_upstream_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.about_source_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = SOURCE_URL,
                modifier = Modifier.clickable {
                    onStartActivity(Intent(Intent.ACTION_VIEW, SOURCE_URL.toUri()))
                },
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.about_upstream_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = UPSTREAM_URL,
                modifier = Modifier.clickable {
                    onStartActivity(Intent(Intent.ACTION_VIEW, UPSTREAM_URL.toUri()))
                },
                color = MaterialTheme.colorScheme.primary
            )
        }

        Section(title = stringResource(R.string.libraries)) {
            Text(
                text = stringResource(R.string.view_full_list),
                modifier = Modifier.clickable(onClick = onViewLibraries),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun ContactLink(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    val resources = LocalResources.current
    val licenseText by remember {
        mutableStateOf(
            resources.openRawResource(R.raw.gpl3)
                .bufferedReader()
                .use { it.readText() }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "GNU General Public License v3.0",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider()

        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp) // TODO check if it's ok
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = licenseText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun CopyLogsButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(stringResource(R.string.copy_logs))
    }
}

@Composable
fun EmailImageButton(
    aboutUiState: AboutUiState,
    onContactWithLastImageClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = aboutUiState.hasLastCapturedImage,
                onClick = onContactWithLastImageClicked
            )
            .alpha(if (aboutUiState.hasLastCapturedImage) 1f else 0.5f)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Email, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(stringResource(R.string.support_last_image))
    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    PaperInTheme {
        val state = AboutUiState(true)
        AboutScreen(state, {}, {}, {}, {}, {})
    }
}