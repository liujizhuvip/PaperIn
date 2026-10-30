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
package com.paperin.scan.data

import java.util.Locale

data class OcrLanguage(
    val code: String,
) {
    val locale: Locale by lazy {
        resolveLocale(code)
    }

    fun displayName(displayLocale: Locale): String =
        locale.getDisplayName(displayLocale)

    companion object {
        private fun verticalVariant(locale: Locale) =
            Locale.Builder().setLocale(locale).setVariant("vertical").build()

        private fun resolveLocale(code: String): Locale {
            val firstPart = code.substringBefore("_")
            return LOCALE_OVERRIDES[code] ?: Locale.Builder().setLanguage(firstPart).build()
        }

        /**
         * Languages whose traineddata files are bundled in the APK under
         * `assets/tessdata`. The list is fixed at build time: PaperIn does not
         * download anything, so an entry that is missing from the assets simply
         * cannot be enabled.
         */
        val AVAILABLE_LANGUAGE_CODES = listOf(
            "chi_sim", "chi_tra", "deu", "eng", "fra", "jpn", "kor", "rus", "spa", "tha", "vie",
        )

        private val LOCALE_OVERRIDES = mapOf(
            // Codes not recognized by Locale.Builder
            "bod" to Locale.forLanguageTag("bo"),   // Tibetan
            "ces" to Locale.forLanguageTag("cs"),   // Czech
            "cym" to Locale.forLanguageTag("cy"),   // Welsh
            "deu" to Locale.GERMAN,
            "ell" to Locale.forLanguageTag("el"),   // Greek
            "eus" to Locale.forLanguageTag("eu"),   // Basque
            "fas" to Locale.forLanguageTag("fa"),   // Persian
            "fra" to Locale.FRENCH,
            "hye" to Locale.forLanguageTag("hy"),   // Armenian
            "isl" to Locale.forLanguageTag("is"),   // Icelandic
            "kat" to Locale.forLanguageTag("ka"),   // Georgian
            "kmr" to Locale.forLanguageTag("ku"),   // Kurdish
            "mkd" to Locale.forLanguageTag("mk"),   // Macedonian
            "mri" to Locale.forLanguageTag("mi"),   // Maori
            "msa" to Locale.forLanguageTag("ms"),   // Malay
            "mya" to Locale.forLanguageTag("my"),   // Burmese
            "nld" to Locale.forLanguageTag("nl"),   // Dutch
            "ron" to Locale.forLanguageTag("ro"),   // Romanian
            "slk" to Locale.forLanguageTag("sk"),   // Slovak
            "sqi" to Locale.forLanguageTag("sq"),   // Albanian
            // Variants pointing to same Locale
            "chi_sim" to Locale.SIMPLIFIED_CHINESE,
            "chi_tra" to Locale.TRADITIONAL_CHINESE,
            "chi_sim_vert" to verticalVariant(Locale.SIMPLIFIED_CHINESE),
            "chi_tra_vert" to verticalVariant(Locale.TRADITIONAL_CHINESE),
            "jpn_vert" to verticalVariant(Locale.JAPANESE),
            "kor_vert" to verticalVariant(Locale.forLanguageTag("ko")),
            "aze_cyrl" to Locale.forLanguageTag("az-Cyrl"),
            "uzb_cyrl" to Locale.forLanguageTag("uz-Cyrl"),
            "srp_latn" to Locale.forLanguageTag("sr-Latn"),
            "deu_latf" to Locale.forLanguageTag("de-1901"),
        )
    }
}
