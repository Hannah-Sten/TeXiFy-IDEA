package nl.hannahsten.texifyidea.run.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.step.CommandLineRunStepParser

class CommandLineRunStepParserTest : BasePlatformTestCase() {

    fun testParserRespectsQuotedSegments() {
        val parts = CommandLineRunStepParser.parse("pythontex \"main file\" --flag=value")

        assertEquals(listOf("pythontex", "main file", "--flag=value"), parts)
    }

    fun testParserDropsBlankSegments() {
        val parts = CommandLineRunStepParser.parse("   ")

        assertTrue(parts.isEmpty())
    }
}
