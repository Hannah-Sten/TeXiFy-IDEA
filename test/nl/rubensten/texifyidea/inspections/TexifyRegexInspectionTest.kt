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
            pattern = Pattern.compile("a.a"),
            replacement = { _, _ -> "The replacement" },
            replacementRange = { IntRange(24, 42) }) {

        // Provide dummy document contents
        var dummyDocument = ""

        /**
         * Inspect the file without the parts of [inspectFile] that require a real PsiFile.
         * For the tests, replacement ranges and replacements could also be done manually but this is slightly easier,
         * because the tests in this file do not aim to test [inspectFile] anyway.
         */
        fun mockInspectFile() : RegexFixes {
            val replacementRanges = arrayListOf<IntRange>()
            val replacements = arrayListOf<String>()
            val groups = arrayListOf<List<String>>()

            val matcher = pattern.matcher(dummyDocument)
            while (matcher.find()) {
                val range = replacementRange(matcher)
                val replacementContent = "Replacement"

                replacementRanges.add(range)
                replacements.add(replacementContent)
                groups.add(groupFetcher(matcher))
            }

            return RegexFixes(
                    quickFixName(matcher),
                    replacements,
                    replacementRanges,
                    groups,
                    this::applyFixes)
        }

        // Remove the ProblemDescriptor part of the applyfix functionality to avoid trying to replace things in a non-existing file
        fun mockApplyFix(replacementRange: IntRange, replacement: String): Int {
            dummyDocument = dummyDocument.replaceRange(replacementRange.start, replacementRange.endInclusive, replacement)
            return replacement.length - replacementRange.length
        }
    }

    @Test
    fun testApplyFixesTwoReplacements() {
        val dummy = MockRegexInspection()
        dummy.dummyDocument = "The words aba and aca should both be replaced in the correct location."

        val fixes = dummy.mockInspectFile()
        val fixFunction = { replacementRange: IntRange, replacement: String, _: List<String> ->
            dummy.mockApplyFix(replacementRange, replacement) }

        dummy.applyFixes(fixFunction, fixes.replacementRanges, fixes.replacements, fixes.groups)

        assertEquals("The words Replacement1 and Replacement2 should both be replaced in the correct location.", dummy.dummyDocument)
    }
}