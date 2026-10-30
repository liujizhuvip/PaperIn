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
package com.paperin.scan.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Palette: clean blue on white ("scanner" look).
// The primary blue is used for actions only; large areas stay white/near-white
// so that document previews are never tinted.
// ---------------------------------------------------------------------------

val Primary = Color(0xFF2B6CFF)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFE7EFFF)
val OnPrimaryContainer = Color(0xFF10389F)
val Secondary = Color(0xFF4E8BFF)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFEDF3FF)
val OnSecondaryContainer = Color(0xFF14315F)
val Tertiary = Color(0xFF00B3A6)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFD6F5F1)
val OnTertiaryContainer = Color(0xFF00322E)
val Error = Color(0xFFE5484D)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFE8E8)
val OnErrorContainer = Color(0xFF4A0B0D)

val Background = Color(0xFFF4F6FB)
val OnBackground = Color(0xFF14171F)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF14171F)
val SurfaceVariant = Color(0xFFEDF1F8)
val OnSurfaceVariant = Color(0xFF6B7280)
val Outline = Color(0xFFDCE3EF)
val OutlineVariant = Color(0xFFEBEFF6)

// Surfaces used by the layered white-on-white layout (cards, sheets, bars).
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF8FAFD)
val SurfaceContainer = Color(0xFFFFFFFF)
val SurfaceContainerHigh = Color(0xFFFFFFFF)
val SurfaceContainerHighest = Color(0xFFF1F4FA)

val PrimaryDark = Color(0xFF7BA5FF)
val OnPrimaryDark = Color(0xFF082B7A)
val PrimaryContainerDark = Color(0xFF16306B)
val OnPrimaryContainerDark = Color(0xFFD7E3FF)
val SecondaryDark = Color(0xFF9DBCFF)
val OnSecondaryDark = Color(0xFF0C2C63)
val SecondaryContainerDark = Color(0xFF1A2942)
val OnSecondaryContainerDark = Color(0xFFD7E3FF)
val TertiaryDark = Color(0xFF5CD6C9)
val OnTertiaryDark = Color(0xFF00312D)
val TertiaryContainerDark = Color(0xFF0E403C)
val OnTertiaryContainerDark = Color(0xFFB8F2EB)
val ErrorDark = Color(0xFFFFB4B4)
val OnErrorDark = Color(0xFF5A0F11)
val ErrorContainerDark = Color(0xFF7A1B1E)
val OnErrorContainerDark = Color(0xFFFFD9D9)

val BackgroundDark = Color(0xFF0E1116)
val OnBackgroundDark = Color(0xFFE7EAF0)
val SurfaceDark = Color(0xFF161A21)
val OnSurfaceDark = Color(0xFFE7EAF0)
val SurfaceVariantDark = Color(0xFF232833)
val OnSurfaceVariantDark = Color(0xFF9AA3B2)
val OutlineDark = Color(0xFF333A47)
val OutlineVariantDark = Color(0xFF232833)

val SurfaceContainerLowestDark = Color(0xFF0B0E13)
val SurfaceContainerLowDark = Color(0xFF12161C)
val SurfaceContainerDark = Color(0xFF161A21)
val SurfaceContainerHighDark = Color(0xFF1B2029)
val SurfaceContainerHighestDark = Color(0xFF222834)

// ---------------------------------------------------------------------------
// Brand-only colors (not part of the Material color scheme).
// ---------------------------------------------------------------------------

object Brand {
    /** Gradient used by the scan header / primary call-to-action surfaces. */
    val GradientStart = Color(0xFF4E8BFF)
    val GradientEnd = Color(0xFF1F5CF6)

    /** Solid version, for the launcher icon and single-color fills. */
    val Blue = Color(0xFF2B6CFF)
    val BlueDeep = Color(0xFF1650E0)
    val BlueTint = Color(0xFFE7EFFF)

    /** Camera screen: dark scrim + white/blue detection overlay. */
    val Scrim = Color(0xCC0B0E14)
    val ScrimSoft = Color(0x990B0E14)
    val ScanFrame = Color(0xFFFFFFFF)
    val ScanFrameDetected = Color(0xFF4E8BFF)
    val Flash = Color(0xFFFFD24A)
}
