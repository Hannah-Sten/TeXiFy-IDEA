package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ExtractRealPackageNameTest : BasePlatformTestCase() {

    fun testTexLiveFrozenDifferentYearEmpty() {
        val output =
            """
            TeX Live 2020 is frozen forever and will no
            longer be updated.  This happens in preparation for a new release.
            
            If you're interested in helping to pretest the new release (when
            pretests are available), please read https://tug.org/texlive/pretest.html.
            Otherwise, just wait, and the new release will be ready in due time.
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            """.trimIndent()

        testPackage(output, null)
    }

    fun testTexLiveFrozenDifferentYearEmptyReverse() {
        val output =
            """
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            TeX Live 2020 is frozen forever and will no
            longer be updated.  This happens in preparation for a new release.
            
            If you're interested in helping to pretest the new release (when
            pretests are available), please read https://tug.org/texlive/pretest.html.
            Otherwise, just wait, and the new release will be ready in due time.
            """.trimIndent()

        println(TexLivePackages.extractRealPackageNameFromOutput(output))
        testPackage(output, null)
    }

    fun testTexLiveFrozenEmpty() {
        val output =
            """
            TeX Live 2019 is frozen forever and will no
            longer be updated.  This happens in preparation for a new release.
            
            If you're interested in helping to pretest the new release (when
            pretests are available), please read https://tug.org/texlive/pretest.html.
            Otherwise, just wait, and the new release will be ready in due time.
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            """.trimIndent()

        testPackage(output, null)
    }

    fun testTexLiveFrozenEmptyReverse() {
        val output =
            """
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            TeX Live 2019 is frozen forever and will no
            longer be updated.  This happens in preparation for a new release.
            
            If you're interested in helping to pretest the new release (when
            pretests are available), please read https://tug.org/texlive/pretest.html.
            Otherwise, just wait, and the new release will be ready in due time.
            """.trimIndent()

        println(TexLivePackages.extractRealPackageNameFromOutput(output))
        testPackage(output, null)
    }

    fun testTexLiveFrozen() {
        val output =
            """
            TeX Live 2019 is frozen forever and will no
            longer be updated.  This happens in preparation for a new release.

            If you're interested in helping to pretest the new release (when
            pretests are available), please read https://tug.org/texlive/pretest.html.
            Otherwise, just wait, and the new release will be ready in due time.
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            rubik:
                    texmf-dist/tex/latex/rubik/rubikrotation.sty
            """.trimIndent()

        testPackage(output, "rubik")
    }

    fun testTexLiveFrozenReverse() {
        val output =
            """
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            rubik:
                    texmf-dist/tex/latex/rubik/rubikrotation.sty
            TeX Live 2019 is frozen forever and will no
            longer be updated.  This happens in preparation for a new release.

            If you're interested in helping to pretest the new release (when
            pretests are available), please read https://tug.org/texlive/pretest.html.
            Otherwise, just wait, and the new release will be ready in due time.
            """.trimIndent()

        testPackage(output, "rubik")
    }

    fun testPackageFound() {
        val output =
            """
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            rubik:
                    texmf-dist/tex/latex/rubik/rubikrotation.sty
            """.trimIndent()

        testPackage(output, "rubik")
    }

    fun testNoPackageFound() {
        val output =
            """
            tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
            """.trimIndent()

        testPackage(output, null)
    }

    private fun testPackage(output: String, expectedPackage: String?) {
        val foundPackage = TexLivePackages.extractRealPackageNameFromOutput(output)
        assertEquals(expectedPackage, foundPackage)
    }
}