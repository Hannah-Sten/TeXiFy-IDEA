package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.SystemInfo
import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.gutter.LatexNavigationGutter
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateFilesets
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class LatexFileNotFoundInspectionTest : TexifyInspectionTestBase(LatexFileNotFoundInspection()) {

    private var absoluteWorkingPath: String

    init {
        val currentRelativePath: Path = Paths.get("")
        absoluteWorkingPath = currentRelativePath.toAbsolutePath().toString().replace(File.separatorChar, '/')
    }

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)

        mockkStatic(LatexNavigationGutter::collectNavigationMarkers)
    }

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/filenotfound"
    }

    fun testMissingAbsolutePath() {
        // Avoid "VfsRootAccess$VfsRootAccessNotAllowedError: File accessed outside allowed roots" on Windows in github actions
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{<error>$absoluteWorkingPath/test/resources/completion/path/myPicture.myinvalidextension</error>}""")
            myFixture.checkHighlighting()
        }
    }

    fun testValidAbsolutePath() {
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/resources/completion/path/myPicture.png}""")

            myFixture.checkHighlighting()
        }
    }

    fun testValidAbsolutePathCaps() {
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/resources/inspections/latex/filenotfound/myOtherPicture.PNG}""")
            myFixture.checkHighlighting()
        }
    }

    fun testBackActionAbsolute() {
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/resources/completion/path/../path/../path/myPicture.png}""")
            myFixture.checkHighlighting()
        }
    }

    fun testCurrDirActionAbsolute() {
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/./resources/./././completion/path/././myPicture.png}""")
            myFixture.checkHighlighting()
        }
    }

    fun testAbsoluteGraphicsDirWithInclude() {
        myFixture.copyFileToProject("myPicture.png")
        myFixture.configureByText(
            LatexFileType,
            """
            \graphicspath{{$absoluteWorkingPath/test/resources/completion/path/}}
            \includegraphics{myPicture.png}
            """.trimIndent()
        )

        myFixture.checkHighlighting()
    }

    fun testUpperCaseAbsoluteGraphicsDirWithInclude() {
        myFixture.copyFileToProject("myOtherPicture.PNG")
        myFixture.configureByText(
            LatexFileType,
            """
            \graphicspath{{$absoluteWorkingPath/test/resources/completion/path/}}
            \includegraphics{myOtherPicture}
            """.trimIndent()
        )

        myFixture.checkHighlighting()
    }

    fun testDefaultExtensionCompletion() {
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/resources/completion/path/myPicture}""")
            myFixture.checkHighlighting()
        }
    }

    fun testDefaultUpperCaseExtensionCompletion() {
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/resources/inspections/latex/filenotfound/myOtherPicture}""")
            myFixture.checkHighlighting()
        }
    }

    fun testDefaultMixedCaseExtensionCompletion() {
        if (!SystemInfo.isWindows) {
            myFixture.configureByText(LatexFileType, """\includegraphics{<error>$absoluteWorkingPath/test/resources/completion/path/myBadPicture</error>}""")

            myFixture.checkHighlighting()
        }
    }

    fun testNoWarningInDefinition() {
        myFixture.configureByText(LatexFileType, """\newcommand*{\gridelement}[1]{\subbottom[#1]{\includegraphics[width=2cm]{media/#1}}}""")

        myFixture.checkHighlighting()
    }

    // Test isn't working
//    @Test
//    fun testImportAbsolutePath() {
//        configureByFiles("ImportPackageAbsolutePath.tex", "chapters/included.tex")
//        myFixture.type("$absoluteWorkingPath/chapters/")
//        myFixture.checkHighlighting()
//    }

    fun testInvalidImportAbsolutePath() {
        myFixture.copyFileToProject("chapters/included.tex")
        myFixture.configureByText(LatexFileType, """\import{/does/not/exist}{<error>included</error>}""")
        myFixture.checkHighlighting()
    }

    fun testImportAbsolutePathIncludedFile() {
        val files = configureByFilesAndBuildFilesets("ImportPackageAbsolutePath.tex", "chapters/included.tex", "chapters/included2.tex")
        myFixture.type("$absoluteWorkingPath/chapters/")
        myFixture.openFileInEditor(files[1].virtualFile)
        myFixture.checkHighlighting()
    }

    fun testImportRelativePathIncludedFile() {
        configureByFilesAndBuildFilesets("chapters/included.tex", "ImportPackageRelativePath.tex", "chapters/included2.tex")

        myFixture.checkHighlighting()
    }

    fun testInvalidImportRelativePathIncludedFile() {
        configureByFilesAndBuildFilesets("chapters/notincluded.tex", "ImportPackageRelativePathInvalid.tex", "chapters/included2.tex")
        val highlightings = myFixture.doHighlighting().filter {
            it.severity == HighlightSeverity.ERROR
        }
        assertEquals(1, highlightings.size)
        assertEquals(highlightings[0].text, "notincluded2.tex")
    }

    fun `test command expansion in root file`() {
        configureByFilesAndBuildFilesets("commandexpansion/main.tex", "commandexpansion/main.bib", "commandexpansion/nest/sub.tex")
        myFixture.checkHighlighting()
    }

    fun `test command expansion in subfile`() {
        configureByFilesAndBuildFilesets("commandexpansion/nest/sub.tex", "commandexpansion/main.tex", "commandexpansion/nest/sub2.tex")
        myFixture.checkHighlighting()
    }

    fun testSubfilesInclusions() {
        configureByFilesAndBuildFilesets("subfilestest/subdir/onedown.tex", "subfilestest/subdir/subsubdir/twodown.tex", "subfilestest/main.tex", "subfilestest/subfiles.cls")
        myFixture.checkHighlighting()
    }

    fun testSubfilesReferenceToMain() {
        myFixture.testHighlighting("subfilestest/subdir/subsubdir/twodown.tex", "subfilestest/subdir/onedown.tex", "subfilestest/main.tex", "subfilestest/subfiles.cls")
    }

// java.lang.Throwable: Stub index points to a file without PSI: file = temp:///src/subfiles/dir1, file type = com.intellij.openapi.fileTypes.UnknownFileType@35570dc3
//    @Test
//    fun `test subfiles`() {
//        configureByFiles("subfiles/dir1/subfile1.tex", "subfiles/main.tex", "subfiles/dir1/dir2/subfile2.tex", "subfiles/dir1/text1.tex", "subfiles/dir1/dir2/text2.tex")
//        myFixture.checkHighlighting()
//    }

    // Test works locally but not on CI
//    fun testLatexmkrc() {
//        configureByFiles("latexmkrc/main.tex", "latexmkrc/.latexmkrc", "latexmkrc/subdir1/mypackage2.sty")
//        myFixture.checkHighlighting()
//    }

    fun testOtherExtension() {
        // Contrary to \includegraphics, \input will accept a file with any extension if specified
        myFixture.addFileToProject("included.txt", "\\LaTeX content")
        myFixture.configureByText(LatexFileType, "\\input{included.txt}")
        myFixture.checkHighlighting()
    }

    fun testPlainInclude() {
        // \input prefers .tex files if they exist
        myFixture.addFileToProject("no-ext", "text content")
        myFixture.configureByText(LatexFileType, "\\input{no-ext}")
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    fun testMultipleDots() {
        // \input prefers .tex files if they exist
        myFixture.addFileToProject("included.v1.0", "text content")
        myFixture.configureByText(LatexFileType, "\\input{included.v1.0}")
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    fun testMultipleDotsTex() {
        // \input prefers .tex files if they exist
        myFixture.addFileToProject("included.v1.0.tex", "\\LaTeX content")
        myFixture.configureByText(LatexFileType, "\\input{included.v1.0}")
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    // Test not working in GitHub Actions
//    fun testCommandAlias() {
//        myFixture.configureByText(LatexFileType, """\newcommand{\myinput}{\input} \myinput{<error descr="File 'doesnotexist.tex' not found">doesnotexist.tex</error>}""")
//        // In practice, this will be triggered by the first something to ask for include commands aliases, for performance reasons
//        updateIncludeCommandsBlocking(myFixture.project)
//        myFixture.checkHighlighting()
//    }
//
//    fun testCommandAliasMoreParameters() {
//        myFixture.configureByText(LatexFileType, """\newcommand{\myinput}[2]{\input{#1}\section{#2}} \myinput{<error descr="File 'doesnotexist.tex' not found">doesnotexist.tex</error>}{My section}""")
//        // In practice, this will be triggered by the first something to ask for include commands aliases, for performance reasons
//        updateIncludeCommandsBlocking(myFixture.project)
//        myFixture.checkHighlighting()
//    }
}