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

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

// A phone lens is soft next to a scanner's optics, and the perspective correction resamples
// every pixel once more. A high-pass of this strength puts the edge of a glyph back where it
// was without ringing: at 0.6 the halo is a few levels deep, invisible at 100 % zoom, while
// the edge slope roughly doubles.
private const val SHARPEN_AMOUNT = 0.6

// Detail below this level is noise, not structure: paper grain, sensor grain, JPEG blocks.
// Leaving it out of the high-pass keeps flat areas flat.
private const val SHARPEN_THRESHOLD = 2.0

// Rows processed per strip. The gain map is local, so a strip only needs a few sigma of
// context; working in strips keeps the float buffers in the low megabytes even at 12 MP
// instead of the hundreds a whole-page float copy would take.
private const val SHARPEN_STRIP_ROWS = 256

/**
 * Unsharp mask, applied after the perspective correction.
 *
 * `out = src + amount * (src - blur(src))`, with the difference below [SHARPEN_THRESHOLD]
 * dropped so that grain is not amplified along with the edges. Paper stays where the
 * whitening put it, print gets its contrast back.
 */
fun sharpenDocument(img: Mat): Mat {
    val rows = img.rows()
    val cols = img.cols()
    if (rows < 3 || cols < 3) return img.clone()

    val sigma = sharpeningSigma(max(cols, rows))
    val halo = ceil(3.0 * sigma).toInt().coerceAtLeast(2)
    val out = Mat(img.size(), img.type())

    var top = 0
    while (top < rows) {
        val bottom = min(top + SHARPEN_STRIP_ROWS, rows)
        // Blur a little beyond the strip: the outermost rows of a blur are computed from
        // reflected pixels, and those must not reach the rows we keep.
        val from = max(0, top - halo)
        val to = min(rows, bottom + halo)

        val strip = img.submat(from, to, 0, cols)
        val blurredStrip = Mat()
        Imgproc.GaussianBlur(strip, blurredStrip, Size(0.0, 0.0), sigma)

        val centreStart = top - from
        val centreEnd = centreStart + (bottom - top)
        val src = strip.submat(centreStart, centreEnd, 0, cols)
        val blurred = blurredStrip.submat(centreStart, centreEnd, 0, cols)
        val dst = out.submat(top, bottom, 0, cols)

        val srcF = Mat()
        src.convertTo(srcF, CvType.CV_32F)
        val blurredF = Mat()
        blurred.convertTo(blurredF, CvType.CV_32F)
        val detail = Mat()
        Core.subtract(srcF, blurredF, detail)
        blurredF.release()

        // Keep the detail that stands out of the noise floor, drop the rest.
        val magnitude = Mat()
        Core.absdiff(detail, Scalar.all(0.0), magnitude)
        val mask = Mat()
        Core.compare(magnitude, Scalar.all(SHARPEN_THRESHOLD), mask, Core.CMP_GT)
        magnitude.release()
        val maskF = Mat()
        mask.convertTo(maskF, CvType.CV_32F, 1.0 / 255.0)
        mask.release()
        Core.multiply(detail, maskF, detail)
        maskF.release()

        val sharpened = Mat()
        Core.addWeighted(srcF, 1.0, detail, SHARPEN_AMOUNT, 0.0, sharpened)
        // convertTo saturates, so a boosted edge clips to black or white instead of wrapping.
        sharpened.convertTo(dst, CvType.CV_8U)

        sharpened.release()
        srcF.release()
        detail.release()
        src.release()
        blurred.release()
        dst.release()
        blurredStrip.release()
        strip.release()

        top = bottom
    }
    return out
}

/**
 * Radius of the high-pass, in pixels.
 *
 * Scaled with the page so that a 300 dpi render is sharpened at the same visual radius as a
 * 150 dpi one: a fixed radius would either do nothing to a large page or cut glyphs in two
 * on a small one. At A4 300 dpi this lands on 1.3 px, the radius a scanner driver uses.
 */
internal fun sharpeningSigma(maxDim: Int): Double =
    (maxDim / 2600.0).coerceIn(0.8, 1.6)
