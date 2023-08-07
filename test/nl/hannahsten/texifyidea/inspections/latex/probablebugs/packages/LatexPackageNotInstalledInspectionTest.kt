package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import io.mockk.every
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.TexLivePackages

class LatexPackageNotInstalledInspectionTest : TexifyInspectionTestBase(LatexPackageNotInstalledInspection()) {

    fun `test no warnings when not using texlive`() {
        texliveWithTlmgr(texlive = false, tlmgr = false)

        testHighlighting("\\usepackage{amsmath}")
    }

    fun `test no warnings when tlmgr is not available`() {
        texliveWithTlmgr(texlive = true, tlmgr = false)

        testHighlighting("\\usepackage{amsmath}")
    }

    fun `test no warnings when package is installed`() {
        texliveWithTlmgr()

        mockkObject(TexLivePackages)
        every { TexLivePackages.packageList } returns mutableListOf("amsmath")

        testHighlighting("\\usepackage{amsmath}")
    }

    private fun texliveWithTlmgr(texlive: Boolean = true, tlmgr: Boolean = true) {
        mockkObject(TexliveSdk)
        every { TexliveSdk.Cache.isAvailable } returns texlive

        mockkObject(LatexSdkUtil)
        every { LatexSdkUtil.isTlmgrInstalled } returns tlmgr
    }
}