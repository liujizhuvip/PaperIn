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
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Modifications Copyright 2026 The PaperIn authors (see NOTICE).
 */
package com.paperin.scan.imageprocessing

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SharpeningTest {

    @Test
    fun radius_grows_with_the_page() {
        // A fixed radius would be invisible on a 12 MP page and would cut glyphs apart on a
        // small one.
        assertThat(sharpeningSigma(1754)).isLessThan(sharpeningSigma(3508))
    }

    @Test
    fun radius_stays_within_a_usable_range() {
        assertThat(sharpeningSigma(600)).isEqualTo(0.8)
        assertThat(sharpeningSigma(20_000)).isEqualTo(1.6)
    }

    @Test
    fun a4_at_300_dpi_lands_near_the_scanner_radius() {
        // A4 at 300 dpi is 3508 px on the long side.
        assertThat(sharpeningSigma(3508)).isBetween(1.2, 1.5)
    }
}
