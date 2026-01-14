package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.file.LatexSourceFileType

class LatexDtxDefinitionIndexerTest : BasePlatformTestCase() {

    class MockContent(val text: String, val name: String) : FileContent {
        override fun <T : Any?> getUserData(key: Key<T>): T? = null
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) {}
        override fun getFileType() = LatexSourceFileType
        override fun getFileName(): String = name
        override fun getFile(): VirtualFile = throw UnsupportedOperationException()
        override fun getProject() = throw UnsupportedOperationException()
        override fun getContent() = ByteArray(0)
        override fun getContentAsText(): CharSequence = text
        override fun getPsiFile() = throw UnsupportedOperationException()
    }

    private fun getDefinitions(text: String, fileName: String = "test.dtx"): List<LatexSimpleDefinition> {
        val map = LatexDtxDefinitionDataIndexer.map(MockContent(text, fileName))
        return map.values.firstOrNull() ?: emptyList()
    }

    fun testOneMacroOneLine() {
        val text = """
            %\begin{macro}{\gram}
            % The gram is an odd unit as it is needed for the base unit kilogram.
            %    \begin{macrocode}
            \DeclareSIUnit \gram { g }
            %    \end{macrocode}
            %\end{macro}
        """.trimIndent()
        val defs = getDefinitions(text, "siunitx.dtx")
        assertEquals(1, defs.size)
        assertEquals("gram", defs[0].name)
        assertTrue(defs[0].description.contains("The gram is an odd unit"))
    }

    fun testOneMacroTwoLines() {
        val text = """
            % \begin{macro}{\MacroTopsep}
            %    Here is the default value for the |\MacroTopsep| parameter
            %    used above.
            %    \begin{macrocode}
            \newskip\MacroTopsep     \MacroTopsep = 7pt plus 2pt minus 2pt
            %    \end{macrocode}
            % \end{macro}
            %
            % \begin{macro}{\ifblank@line}
            % \begin{macro}{\blank@linetrue}
            % \begin{macro}{\blank@linefalse}
            %    |\ifblank@line| is the switch used in the definition above.
            %    In the original \textsf{verbatim} environment the |\if@tempswa|
            %    switch is used. This is dangerous because its value may change
            %    while processing lines in the \textsf{macrocode} environment.
            %    \begin{macrocode}
            \newif\ifblank@line
            %    \end{macrocode}
            % \end{macro}
            % \end{macro}
            % \end{macro}
            %
        """.trimIndent()
        val defs = getDefinitions(text, "doc.dtx")
        val names = defs.map { it.name }
        assertTrue("MacroTopsep" in names)
        val macro = defs.find { it.name == "MacroTopsep" }!!
        assertTrue(macro.description.contains("Here is the default value"))
    }

    fun testDoubleMacroDefinition() {
        val text = """
            %
            %
            % \begin{macro}{\MacrocodeTopsep}
            % \begin{macro}{\MacroIndent}
            %    In the code above, we have used two registers. Therefore we have
            %    to allocate them. The default values might be overwritten with
            %    the help of the |\DocstyleParms| macro.
            % \changes{v1.5s}{1989/11/05}{Support for code line no. (Undoc)}
            % \changes{v1.5y}{1990/02/24}{Default changed.}
            % \changes{v1.6b}{1990/06/15}{\cs{rm} moved before \cs{scriptsize} to avoid unnecessary fontwarning.}
            %    \begin{macrocode}
            \newskip\MacrocodeTopsep \MacrocodeTopsep = 3pt plus 1.2pt minus 1pt
            \newdimen\MacroIndent
            \settowidth\MacroIndent{\rmfamily\scriptsize 00\ }
            %    \end{macrocode}
            % \end{macro}
            % \end{macro}
            %
            %
        """.trimIndent()
        val defs = getDefinitions(text, "doc.dtx")
        val names = defs.map { it.name }
        assertTrue("MacrocodeTopsep" in names)
        assertTrue("MacroIndent" in names)
        val desc = defs.find { it.name == "MacrocodeTopsep" }!!.description
        assertEquals(desc, defs.find { it.name == "MacroIndent" }!!.description)
    }

    fun testDescribeMacro() {
        val text = """
            % \subsection{Describing the usage of new macros}
            %
            % \DescribeMacro\DescribeMacro
            % \changes{v1.7c}{1992/03/26}{Added.}
            % When you describe a new macro you may use |DescribeMacro| to
            % indicate that at this point the usage of a specific macro is
            % explained. It takes one argument which will be printed in the margin
            % and also produces a special index entry.  For example, I used
            % <redacted> to make clear that this is the
            % point where the usage of |DescribeMacro| is explained.
            %
            % \DescribeMacro{\DescribeEnv}
            % An analogous macro |\DescribeEnv| should be used to indicate
            % that a \LaTeX{} environment is explained. It will produce a somewhat
            % different index entry. Below I used |\DescribeEnv{verbatim}|.
            %
        """.trimIndent()
        val defs = getDefinitions(text, "doc.dtx")
        val names = defs.map { it.name }
        assertTrue("DescribeMacro" in names)
        assertTrue("DescribeEnv" in names)
        assertTrue(defs.find { it.name == "DescribeMacro" }!!.description.contains("When you describe a new macro"))
        assertTrue(defs.find { it.name == "DescribeEnv" }!!.description.contains("An analogous macro"))
    }

    fun testDescribeMacroWithParameters() {
        val text = """
            % The text returned by Python must be valid \LaTeX\ code. (...)
            %
            % \DescribeMacro{\pyc\oarg{session}\meta{opening~delim}\meta{code}\meta{closing~delim}}
            % This command is used for executing but not typesetting \meta{code} (...)
            %
            % \DescribeMacro{\pys\oarg{session}\meta{opening~delim}\meta{code}\meta{closing~delim}}
        """.trimIndent()
        val defs = getDefinitions(text, "pythontex.dtx")
        val pyc = defs.find { it.name == "pyc" }!!
        assertTrue(pyc.arguments.isNotEmpty())
        assertTrue(pyc.description.contains("This command is used"))
    }

    fun testDescribeEnv() {
        val text = """
            % different index entry. Below I used |\DescribeEnv{verbatim}|.
            %
            % \DescribeEnv{verbatim}
            % It is often a good idea to include examples of the usage of new macros
            % in the text. Because of the |%| sign in the first column of every
            % row, the \textsf{verbatim} environment is slightly altered to suppress
            % those
            % characters.\footnote{These macros were written by Rainer
            %                      Sch\"opf~\cite{art:verbatim}. He also
            %                      provided a new \textsf{verbatim} environment
            %                      which can be used inside of other macros.}
            %
        """.trimIndent()
        val defs = getDefinitions(text, "doc.dtx")
        assertTrue(defs.any { it.name == "verbatim" })
        assertTrue(defs.find { it.name == "verbatim" }!!.isEnv)
    }

    fun testNewEnvironment() {
        val text = """
 %
 %
 % \subsection{Macros surrounding the `definition parts'}
 %
 % \begin{environment}{macrocode}
 %    Parts of the macro definition will be surrounded by the
 %    environment \textsf{macrocode}.  Put more precisely, they will be
 %    enclosed by a macro whose argument (the text to be set
 %    `verbatim') is terminated by the string
 % \verb*+%    \end{macrocode}+.  Carefully note the number of spaces.
 % \changes{v1.5r}{1989/11/04}{Support for code line no. (Undoc)}
 %    \begin{macrocode}
 \def\macrocode{\macro@code
 ...
 %    \end{macrocode}
 % \end{environment}
 %
 %
        """.trimIndent()
        val defs = getDefinitions(text, "doc.dtx")
        assertTrue(defs.any { it.name == "macrocode" && it.isEnv })
    }

    fun testExtractArgumentsRequired() {
        // pict2e.dtx
        // Note that here a trick is used to align the commands with the arguments, by using that the macro's will appear in the left margin below each other
        val text = """
            \circle\marg{DIAM}\\ \circle*\marg{DIAM}\\ The (hollow) circles and disks (filled circles) of the \SL\ ^^A implementation had severe restrictions on the number of different diameters and maximum diameters available.
        """.trimIndent()
        val args = LatexDtxDefinitionDataIndexer.parseArguments(text)
        assertEquals("Number of arguments:", 1, args.size)
        assertEquals("DIAM", args[0].name)
        assertTrue(args[0].isRequired)
    }

    fun testExtractArgumentsMultipleRequired() {
        // pict2e.dtx
        val text = """
            \moveto\parg{X,Y}\\ \lineto\parg{X,Y}\\ \curveto\parg{X2,Y2}\parg{X3,Y3}\parg{X4,Y4}\\ \circlearc\oarg{N}\marg{X}\marg{Y}\marg{RAD}\marg{ANGLE1}\marg{ANGLE2}\\ These commands directly correspond to the \PS\ and \PDF\ path operators. You start defining a path giving its initial point by \cmd{\moveto}. Then you can consecutively add a line segment to a given point by \cmd{\lineto}, a ...
        """.trimIndent()
        val args = LatexDtxDefinitionDataIndexer.parseArguments(text)
        assertEquals("Number of arguments:", 6, args.size)
        assertEquals("N", args[0].name)
        assertTrue(args[0].isOptional)
        assertEquals("X", args[1].name)
        assertTrue(args[1].isRequired)
        assertEquals("ANGLE2", args[5].name)
    }

    fun testExtractArgumentsOneLineWithoutCommandPrefix() {
        // xcolor.dtx
        // Also seems common, to not repeat the command but start with the arguments list.
        val text = """
            \oarg{type}\marg{model-list}\marg{head}\marg{tail}\marg{set spec}\\ This command facilitates the construction of \emph{\Index{color set}s}, i.e.~(possibly large) sets of individual colors with common underlying \Meta{model-list} and \Meta{type}. Here, \Meta{set spec} = \Meta[1]{name},\Meta[1]{spec-list};\dots;\Meta[l]{name},\Meta[l]{spec-list} ( name/specification-list pairs).
        """.trimIndent()
        val args = LatexDtxDefinitionDataIndexer.parseArguments(text)
        assertEquals("Number of arguments:", 5, args.size)
        assertEquals("type", args[0].name)
        assertTrue(args[0].isOptional)
        assertEquals("model-list", args[1].name)
        assertTrue(args[1].isRequired)
        assertEquals("set spec", args[4].name)
    }
}
