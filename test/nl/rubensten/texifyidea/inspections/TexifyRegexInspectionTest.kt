package nl.rubensten.texifyidea.inspections

import nl.rubensten.texifyidea.util.length
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class TexifyRegexInspectionTest {

    class MockRegexInspection : TexifyRegexInspection(
            inspectionDisplayName = "",
            myInspectionId = "",
            errorMessage = { "" },
            pattern = Pattern.compile("aaa.aaa"),
            replacement = { _, _ -> "The replacement" },
            replacementRange = { IntRange(24, 42) }) {

        // Provide dummy document contents
        val dummyDocument = "The words aaabaaa and aaacaaa should both be replaced in the correct location."

        // Remove the first part of the applyfix functionality to avoid trying to replace things in a non-existing file
        fun mockApplyFix(replacementRange: IntRange, replacement: String): Int {
            dummyDocument.replaceRange(replacementRange.start, replacementRange.endInclusive, replacement)
            return replacement.length - replacementRange.length
        }
    }

    @Test
    fun testApplyFixesTwoReplacements() {
        val dummy = MockRegexInspection()
        val replacementRanges = arrayListOf(IntRange(10, 17), IntRange(22, 28))
        val replacements = arrayListOf("Replacement1", "Replacement2")
        val groupsList = arrayListOf<List<String>>(arrayListOf("aaabaaa"), arrayListOf("aaacaaa"))

        dummy.applyFixes({ replacementRange, replacement, _ -> dummy.mockApplyFix(replacementRange, replacement) }, replacementRanges, replacements, groupsList)

        assertEquals("The words Replacement1 and Replacement2 should both be replaced in the correct location.", dummy.dummyDocument)
    }
}