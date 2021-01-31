package nl.hannahsten.texifyidea.lang

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.OptionalArgument
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument

class LatexCommandTest : BasePlatformTestCase() {

    fun testExtractArgumentsRequired() {
        // pict2e.dtx
        // Note that here a trick is used to align the commands with the arguments, by using that the macro's will appear in the left margin below each other
        val text = """
            \circle\marg{DIAM}\\ \circle*\marg{DIAM}\\ The (hollow) circles and disks (filled circles) of the \SL\ ^^A implementation had severe restrictions on the number of different diameters and maximum diameters available.
        """.trimIndent()
        val args = LatexCommand.extractArgumentsFromDocs(text, "\\circle")
        assertEquals("Number of arguments:", 1, args.size)
        assertEquals(RequiredArgument("DIAM"), args[0])
    }

    fun testExtractArgumentsMultipleRequired() {
        // pict2e.dtx
        val text = """
            \moveto\parg{X,Y}\\ \lineto\parg{X,Y}\\ \curveto\parg{X2,Y2}\parg{X3,Y3}\parg{X4,Y4}\\ \circlearc\oarg{N}\marg{X}\marg{Y}\marg{RAD}\marg{ANGLE1}\marg{ANGLE2}\\ These commands directly correspond to the \PS\ and \PDF\ path operators. You start defining a path giving its initial point by \cmd{\moveto}. Then you can consecutively add a line segment to a given point by \cmd{\lineto}, a ...
        """.trimIndent()
        val args = LatexCommand.extractArgumentsFromDocs(text, "\\circlearc")
        assertEquals("Number of arguments:", 6, args.size)
        assertEquals(OptionalArgument("N"), args[0])
        assertEquals(RequiredArgument("X"), args[1])
        assertEquals(RequiredArgument("ANGLE2"), args[5])
    }

    fun testExtractArgumentsOneLineWithoutCommandPrefix() {
        // xcolor.dtx
        // Also seems common, to not repeat the command but start with the arguments list.
        val text = """
            \oarg{type}\marg{model-list}\marg{head}\marg{tail}\marg{set spec}\\ This command facilitates the construction of \emph{\Index{color set}s}, i.e.~(possibly large) sets of individual colors with common underlying \Meta{model-list} and \Meta{type}. Here, \Meta{set spec} = \Meta[1]{name},\Meta[1]{spec-list};\dots;\Meta[l]{name},\Meta[l]{spec-list} ( name/specification-list pairs).
        """.trimIndent()
        val args = LatexCommand.extractArgumentsFromDocs(text, "\\definecolorset")
        assertEquals("Number of arguments:", 5, args.size)
        assertEquals(OptionalArgument("type"), args[0])
        assertEquals(RequiredArgument("model-list"), args[1])
        assertEquals(RequiredArgument("set spec"), args[4])
    }

    fun testLatexBaseMetaArguments() {
        // base/ltsect.dtx
        // No clue why they didn't use the doc package.
        val text = """
            The \contentsline{\meta{type}}{\meta{entry}}{\meta{page}} macro produces a \meta{type} entry in a table of contents, etc.
        """.trimIndent()
        val args = LatexCommand.extractArgumentsFromDocs(text, "\\contentsline")
        assertEquals("Number of arguments:", 3, args.size)
        assertEquals(RequiredArgument("type"), args[0])
        assertEquals(RequiredArgument("entry"), args[1])
        assertEquals(RequiredArgument("page"), args[2])
    }
}