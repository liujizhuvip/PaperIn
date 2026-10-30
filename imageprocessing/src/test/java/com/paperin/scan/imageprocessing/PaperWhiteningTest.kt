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
package com.paperin.scan.imageprocessing

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PaperWhiteningTest {

    @Test
    fun kernel_is_always_odd() {
        for (workDim in 3..2000 step 7) {
            assertThat(paperEstimateKernel(workDim) % 2).isEqualTo(1)
        }
    }

    @Test
    fun kernel_stays_within_sane_bounds() {
        assertThat(paperEstimateKernel(10)).isEqualTo(5)
        assertThat(paperEstimateKernel(100_000)).isEqualTo(15)
    }

    @Test
    fun kernel_grows_with_the_working_size() {
        // A wider working image needs a wider kernel to still bridge a line of print.
        assertThat(paperEstimateKernel(200)).isLessThan(paperEstimateKernel(400))
    }
}
