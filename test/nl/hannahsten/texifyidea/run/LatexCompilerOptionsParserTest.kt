package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.LatexCommandLineOptionsCache

class LatexCompilerOptionsParserTest : BasePlatformTestCase() {

    fun testPdflatex() {
        val output = """
            Usage: pdftex [OPTION]... [TEXNAME[.tex]] [COMMANDS]
               or: pdftex [OPTION]... \FIRST-LINE
               or: pdftex [OPTION]... &FMT ARGS
              Run pdfTeX on TEXNAME, usually creating TEXNAME.pdf.

              If no arguments or options are specified, prompt for input.
            (...)
            -cnf-line=STRING        parse STRING as a configuration file line
            -etex                   enable e-TeX extensions
            [-no]-file-line-error   disable/enable file:line:error style messages
        """.trimIndent()
        val options = LatexCommandLineOptionsCache.parseHelpOutput("pdflatex", output)
        assertEquals(
            listOf(
                Pair("cnf-line=STRING", "parse STRING as a configuration file line"),
                Pair("etex", "enable e-TeX extensions"),
                Pair("file-line-error", "disable/enable file:line:error style messages"),
                Pair("no-file-line-error", "disable/enable file:line:error style messages"),
            ),
            options
        )
    }

    fun testLualatex() {
        val output = """
            Usage: luahbtex --lua=FILE [OPTION]... [TEXNAME[.tex]] [COMMANDS]
               or: luahbtex --lua=FILE [OPTION]... \FIRST-LINE
               or: luahbtex --lua=FILE [OPTION]... &FMT ARGS
              Run LuaHBTeX on TEXNAME, usually creating TEXNAME.pdf.
                (...)
                
               --cnf-line =STRING            parse STRING as a configuration file line
               --debug-format                enable format debugging
               --[no-]file-line-error        disable/enable file:line:error style messages

        """.trimIndent()
        val options = LatexCommandLineOptionsCache.parseHelpOutput("lualatex", output)
        assertEquals(
            listOf(
                // I don't know, maybe a typo in lualatex?
                Pair("cnf-line", "=STRING            parse STRING as a configuration file line"),
                Pair("debug-format", "enable format debugging"),
                Pair("file-line-error", "disable/enable file:line:error style messages"),
                Pair("no-file-line-error", "disable/enable file:line:error style messages"),
            ),
            options
        )
    }

    fun testLatexmk() {
        val output = """
            Latexmk 4.83: Automatic LaTeX document generation routine
            
            Usage: latexmk [latexmk_options] [filename ...]
            
              Latexmk_options:
               -bibtex       - use bibtex when needed (default)
               -bibtex-      - never use bibtex
               -bibfudge- or -bibtexfudge- - don't change directory when running bibtex
        """.trimIndent()
        val options = LatexCommandLineOptionsCache.parseHelpOutput("latexmk", output)
        assertEquals(
            listOf(
                Pair("bibtex", "use bibtex when needed (default)"),
                Pair("bibtex-", "never use bibtex"),
                Pair("bibfudge-", "don't change directory when running bibtex"),
                Pair("bibtexfudge-", "don't change directory when running bibtex"),
            ),
            options
        )
    }
}