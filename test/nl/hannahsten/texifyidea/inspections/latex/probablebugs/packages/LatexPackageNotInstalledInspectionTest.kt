package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.TexLivePackages

class LatexPackageNotInstalledInspectionTest : TexifyInspectionTestBase(LatexPackageNotInstalledInspection()) {

    fun `test no warnings when not using texlive`() {
        texliveWithTlmgr(texlive = false, tlmgr = false)

        testHighlighting("\\usepackage{amsmath}")

        unmockkObject(TexliveSdk.Cache)
        unmockkObject(LatexSdkUtil)
    }

    fun `test no warnings when package is installed`() {
        texliveWithTlmgr()

        mockkObject(TexLivePackages)
        every { TexLivePackages.packageList } returns mutableListOf("amsmath")

        testHighlighting("\\usepackage{amsmath}")

        unmockkObject(TexliveSdk.Cache)
        unmockkObject(LatexSdkUtil)
        unmockkObject(TexLivePackages)
    }

    private fun texliveWithTlmgr(texlive: Boolean = true, tlmgr: Boolean = true) {
        mockkObject(TexliveSdk.Cache)
        every { TexliveSdk.Cache.isAvailable } returns texlive

        mockkObject(LatexSdkUtil)
        every { LatexSdkUtil.isTlmgrAvailable(any()) } returns tlmgr
    }
}