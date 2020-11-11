package nl.hannahsten.texifyidea.inspections.latex

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.settings.LatexSdk
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.TexLivePackages
import nl.hannahsten.texifyidea.util.runCommand

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

    fun `test warning when package is not installed`() {
        texliveWithTlmgr()

        mockkObject(TexLivePackages)
        every { TexLivePackages.packageList } returns mutableListOf()

        // The package is not installed locally.
        mockkStatic("nl.hannahsten.texifyidea.util.StringsKt")
        every { "tlmgr search --file /amsmath.sty".runCommand() } returns ""

        testHighlighting("<warning descr=\"Package is not installed or \\ProvidesPackage is missing\">\\usepackage{amsmath}</warning>")
    }

    private fun texliveWithTlmgr(texlive: Boolean = true, tlmgr: Boolean = true) {
        mockkObject(LatexSdk)
        every { LatexSdk.isTexliveAvailable } returns texlive

        mockkObject(SystemEnvironment)
        every { SystemEnvironment.isTlmgrInstalled } returns tlmgr
    }
}