package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import io.mockk.every
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.TexLivePackages

class LatexPackageUpdateInspectionTest : TexifyInspectionTestBase(LatexPackageUpdateInspection()) {

    fun testWarning() {
        texliveWithTlmgr()

        mockkObject(TexLivePackages)
        LatexPackageUpdateInspection.Cache.availablePackageUpdates = mapOf(Pair("amsmath", Pair("71408", "72779")))

        testHighlighting("<warning descr=\"Update available for package amsmath\">\\usepackage{amsmath}</warning>")
    }

    private fun texliveWithTlmgr(texlive: Boolean = true, tlmgr: Boolean = true) {
        mockkObject(TexliveSdk.Cache)
        every { TexliveSdk.Cache.isAvailable } returns texlive

        mockkObject(LatexSdkUtil)
        every { LatexSdkUtil.isTlmgrInstalled } returns tlmgr
    }
}