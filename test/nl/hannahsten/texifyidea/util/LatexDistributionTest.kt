package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test [LatexSdk].
 */
class LatexDistributionTest {

    @Test
    fun testTexlive() {
        val output =
            """
            pdfTeX 3.14159265-2.6-1.40.20 (TeX Live 2019)
            kpathsea version 6.3.1
            Copyright 2019 Han The Thanh (pdfTeX) et al.
            There is NO warranty.  Redistribution of this software is
            covered by the terms of both the pdfTeX copyright and
            the Lesser GNU General Public License.
            For more information about these matters, see the file
            named COPYING and the pdfTeX source.
            Primary author of pdfTeX: Han The Thanh (pdfTeX) et al.
            Compiled with libpng 1.6.36; using libpng 1.6.36
            Compiled with zlib 1.2.11; using zlib 1.2.11
            Compiled with xpdf version 4.01
            """.trimIndent()

        assertEquals("TeX Live 2019", LatexSdkUtil.parsePdflatexOutput(output))
    }

    @Test
    fun testMiktex() {
        val output =
            """
            MiKTeX-pdfTeX 2.9.6870 (1.40.19) (MiKTeX 2.9.6880 64-bit)
            Copyright (C) 1982 D. E. Knuth, (C) 1996-2018 Han The Thanh
            TeX is a trademark of the American Mathematical Society.
            using bzip2 version 1.0.6, 6-Sept-2010
            compiled with curl version 7.61.1; using libcurl/7.61.1 WinSSL
            compiled with expat version 2.2.6; using expat_2.2.6
            compiled with jpeg version 9.3
            compiled with liblzma version 50020042; using 50020042
            compiled with libpng version 1.6.35; using 1.6.35
            compiled with libressl version LibreSSL 2.8.2; using LibreSSL 2.8.2
            compiled with MiKTeX Application Framework version 4.6888; using 4.6888
            compiled with MiKTeX Core version 10.6888; using 10.6888
            compiled with MiKTeX Archive Extractor version 1.6882; using 1.6882
            compiled with MiKTeX Package Manager version 4.6888; using 4.6888
            compiled with poppler version 0.60.1
            compiled with uriparser version 0.8.6
            compiled with zlib version 1.2.11; using 1.2.11
            """.trimIndent()

        assertEquals("MiKTeX 2.9.6880 64-bit", LatexSdkUtil.parsePdflatexOutput(output))
    }
}