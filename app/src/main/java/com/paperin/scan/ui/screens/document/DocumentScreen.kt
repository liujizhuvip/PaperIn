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
package com.paperin.scan.ui.screens.document

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.zoomable
import com.paperin.scan.R
import com.paperin.scan.domain.PageViewKey
import com.paperin.scan.domain.Rotation
import com.paperin.scan.ui.Navigation
import com.paperin.scan.ui.components.CommonPageListState
import com.paperin.scan.ui.components.ConfirmationDialog
import com.paperin.scan.ui.components.CountBadge
import com.paperin.scan.ui.components.GhostActionButton
import com.paperin.scan.ui.components.MainActionButton
import com.paperin.scan.ui.components.MyScaffold
import com.paperin.scan.ui.components.SecondaryActionButton
import com.paperin.scan.ui.dummyNavigation
import com.paperin.scan.ui.fakeDocument
import com.paperin.scan.ui.fakeImage
import com.paperin.scan.ui.theme.PaperInTheme
import com.paperin.scan.imageprocessing.ColorMode
import com.paperin.scan.imageprocessing.ColorMode.BLACK_AND_WHITE
import com.paperin.scan.imageprocessing.ColorMode.COLOR
import com.paperin.scan.imageprocessing.ColorMode.GRAYSCALE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    uiState: DocumentUiState,
    navigation: Navigation,
    onExportClick: () -> Unit,
    onDeleteImage: () -> Unit,
    onRotateImage: (Boolean) -> Unit,
    onColorModeSelected: (ColorMode) -> Unit,
    onCropClick: () -> Unit,
    onPageReorder: (String, Int) -> Unit,
    onPageSelected: (Int) -> Unit,
) {
    val showDeletePageDialog = rememberSaveable { mutableStateOf(false) }

    val document = uiState.document
    val currentPageIndex = uiState.currentPageIndex
    BackHandler { navigation.back() }

    val listState = rememberLazyListState()
    LaunchedEffect(currentPageIndex) {
        listState.scrollToItem(currentPageIndex)
    }

    MyScaffold(
        navigation = navigation,
        pageListState = CommonPageListState(
            document,
            onPageClick = { index -> onPageSelected(index) },
            onPageReorder = onPageReorder,
            currentPageIndex = currentPageIndex,
            listState = listState,
            showPageNumbers = true,
        ),
        bottomBar = {
            BottomBar(onExportClick, navigation.toCameraScreen)
        },
    ) { modifier ->
        DocumentPreview(
            uiState,
            { showDeletePageDialog.value = true },
            onRotateImage,
            onColorModeSelected,
            onCropClick,
            modifier
        )
        if (showDeletePageDialog.value) {
            ConfirmationDialog(
                title = stringResource(R.string.delete_page),
                message = stringResource(R.string.delete_page_warning),
                showDialog = showDeletePageDialog
            ) { onDeleteImage() }
        }
    }
}

@Composable
private fun DocumentPreview(
    uiState: DocumentUiState,
    onDeleteImage: () -> Unit,
    onRotateImage: (Boolean) -> Unit,
    onColorModeSelected: (ColorMode) -> Unit,
    onCropClick: () -> Unit,
    modifier: Modifier,
) {
    val currentPageIndex = uiState.currentPageIndex
    val document = uiState.document
    Column (
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box (
            modifier = Modifier.fillMaxSize()
        ) {
            val bitmap = uiState.currentPage?.bitmap
            val pageKey = uiState.currentPage?.key
            if (bitmap != null && pageKey != null) {
                val imageBitmap = bitmap.asImageBitmap()
                val zoomState = remember(pageKey) {
                    ZoomState(
                        contentSize = Size(bitmap.width.toFloat(), bitmap.height.toFloat())
                    )
                }

                Box(modifier = Modifier
                    .fillMaxSize(0.9f)
                    .align(Alignment.Center)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(10.dp)) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(12.dp))
                            .zoomable(zoomState)
                    )
                }
            }
            if (uiState.currentPage?.isLoading ?: false) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            EditButtons(
                uiState,
                onColorModeSelected,
                onCropClick,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            RotationButtons(onRotateImage, Modifier.align(Alignment.BottomCenter))
            SecondaryActionButton(
                Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete_page),
                onClick = { onDeleteImage() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
            CountBadge(
                text = "${currentPageIndex + 1} / ${document.pageCount()}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun RotationButtons(
    onRotateImage: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // RotateLeft on the left, RotateRight on the right: for both LTR and RTL languages
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier = modifier.padding(8.dp)) {
            // Using AutoMirrored icons would lead to an opposite rotation in RTL languages
            @Suppress("DEPRECATION")
            SecondaryActionButton(
                icon = Icons.Default.RotateLeft,
                contentDescription = stringResource(R.string.rotate_left),
                onClick = { onRotateImage(false) }
            )
            Spacer(Modifier.width(8.dp))
            @Suppress("DEPRECATION")
            SecondaryActionButton(
                icon = Icons.Default.RotateRight,
                contentDescription = stringResource(R.string.rotate_right),
                onClick = { onRotateImage(true) }
            )
        }
    }
}

@Composable
fun EditButtons(
    uiState: DocumentUiState,
    onColorModeSelected: (ColorMode) -> Unit,
    onCropClick: () -> Unit,
    modifier: Modifier
) {
    Row(modifier = modifier.padding(8.dp)) {
        uiState.currentPage?.colorMode?.let {
            ColorModeButton(
                currentColorMode = it,
                onColorModeSelected = onColorModeSelected,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (uiState.currentPage?.canBeCropped ?: false) {
            SecondaryActionButton(
                icon = Icons.Default.Crop,
                contentDescription = stringResource(R.string.crop),
                onClick = onCropClick,
            )
        }
    }
}

@Composable
fun ColorModeButton(
    currentColorMode: ColorMode,
    onColorModeSelected: (ColorMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        SecondaryActionButton(
            icon = Icons.Default.AutoFixHigh,
            contentDescription = stringResource(R.string.color_mode),
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ColorMode.entries.forEach { colorMode ->
                DropdownMenuItem(
                    text = { Text(stringResource(colorMode.labelResource)) },
                    onClick = {
                        onColorModeSelected(colorMode)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentColorMode == colorMode) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

private val ColorMode.labelResource: Int
    get() = when (this) {
        COLOR -> R.string.color_mode_color
        GRAYSCALE -> R.string.color_mode_grayscale
        BLACK_AND_WHITE -> R.string.color_mode_black_and_white
    }

@Composable
private fun BottomBar(
    onExportClick: () -> Unit,
    onAddPageClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GhostActionButton(
            onClick = onAddPageClick,
            text = stringResource(R.string.add_page),
            icon = Icons.Outlined.Add,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(12.dp))
        MainActionButton(
            onClick = onExportClick,
            icon = Icons.Default.Done,
            text = stringResource(R.string.export),
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
@Preview
@Preview(locale = "ar")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Landscape", showBackground = true, widthDp = 640, heightDp = 320)
fun DocumentScreenPreview() {
    PaperInTheme {
        val image = fakeImage("gallica.bnf.fr-bpt6k5530456s-1", LocalContext.current).toBitmap()
        val document = fakeDocument(
            listOf(1, 2).map { "gallica.bnf.fr-bpt6k5530456s-$it" }.toImmutableList(),
            LocalContext.current
        )
        val key = PageViewKey("123", Rotation.R0, null, 0)
        DocumentScreen(
            uiState = DocumentUiState(1, CurrentPageUiState(key,image, COLOR, true), document),
            navigation = dummyNavigation(),
            onExportClick = {},
            onDeleteImage = { },
            onRotateImage = { _ -> },
            onColorModeSelected = { },
            onCropClick = { },
            onPageReorder = { _,_ -> },
            onPageSelected = { _ -> },
        )
    }
}
