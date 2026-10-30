/*
 * Copyright 2025-2026 The FairScan authors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Modifications Copyright 2026 The PaperIn authors (see NOTICE).
 */
package com.paperin.scan.domain

import com.paperin.scan.R

/**
 * Output size and JPEG quality of a rendered page.
 *
 * The pixel counts are quoted for A4, which is what a page of print usually is:
 *
 * | | pixels | A4 |
 * | --- | --- | --- |
 * | LOW | 2.0 MP | ~150 dpi |
 * | BALANCED | 8.7 MP | ~300 dpi |
 * | HIGH | 12.0 MP | ~350 dpi |
 *
 * 300 dpi is what a flatbed scanner produces by default, and BALANCED used to sit at 2 MP
 * (~150 dpi): a page of 10 pt print lost about half its stroke detail on the way out, which
 * is what made the result look soft next to a scanned sheet. Stored pages are therefore
 * several times larger than they were; HIGH no longer has to invent resolution, it renders
 * at the capture resolution.
 */
enum class ExportQuality(
    val jpegQuality: Int,
    val maxPixels: Long,
    val labelResource: Int
) {
    LOW(
        jpegQuality = 70,
        maxPixels = 2_000_000,
        R.string.export_quality_low,
    ),
    BALANCED(
        jpegQuality = 85,
        maxPixels = 8_700_000,
        R.string.export_quality_balanced,
    ),
    HIGH(
        jpegQuality = 92,
        maxPixels = 12_000_000,
        R.string.export_quality_high,
    ),
}
