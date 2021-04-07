package nl.hannahsten.texifyidea.inspections

import nl.hannahsten.texifyidea.util.length
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class TexifyRegexInspectionTest {

    // Implement a mock inspection to test inherited methods.
    class MockRegexInspection : TexifyRegexInspection(
        inspectionDisplayName = "",
        inspectionId = "",
        errorMessage = { "" },
        pattern = Pattern.compile("bl.b")
    ) {

        // Provide dummy document contents
        var dummyDocument = ""

        /**
         * Inspect the file without the parts of [inspectFile] that require a real PsiFile.
         * For the tests, replacement ranges and replacements could also be done manually but this is slightly easier,
         * because the tests in this file do not aim to test [inspectFile] anyway.
         */
        fun mockInspectFile(replacementContent: String = "replacement"): RegexFixes {
            val replacementRanges = arrayListOf<IntRange>()
            val replacements = arrayListOf<String>()
            val groups = arrayListOf<List<String>>()

            val matcher = pattern.matcher(dummyDocument)
            while (matcher.find()) {
                replacementRanges.add(replacementRange(matcher))
                replacements.add(replacementContent)
                groups.add(groupFetcher(matcher))
            }

            return RegexFixes(
                quickFixName(matcher),
                replacements,
                replacementRanges,
                groups,
                this::applyFixes
            )
        }

        // Remove the ProblemDescriptor part of the applyfix functionality to avoid trying to replace things in a non-existing file
        fun mockApplyFix(replacementRange: IntRange, replacement: String): Int {
            dummyDocument = dummyDocument.replaceRange(replacementRange.start, replacementRange.endInclusive, replacement)
            return replacement.length - replacementRange.length
        }
    }

    /**
     * Test replacements that have a larger length than the original text.
     */
    @Test
    fun testApplyFixesTwoLargerReplacements() {
        val dummy = MockRegexInspection()
        dummy.dummyDocument = "This sentence contains one blub and another blab which are both the same."

        val fixes = dummy.mockInspectFile()
        val fixFunction = { replacementRange: IntRange, replacement: String, _: List<String> ->
            dummy.mockApplyFix(replacementRange, replacement)
        }

        dummy.applyFixes(fixFunction, fixes.replacementRanges, fixes.replacements, fixes.groups)

        assertEquals("This sentence contains one replacement and another replacement which are both the same.", dummy.dummyDocument)
    }

    /**
     * Test replacements that have a larger length than the original text.
     */
    @Test
    fun testApplyFixesTwoSmallerReplacements() {
        val dummy = MockRegexInspection()
        dummy.dummyDocument = "This sentence contains one blub and another blab which are both the same."

        val fixes = dummy.mockInspectFile("r")
        val fixFunction = { replacementRange: IntRange, replacement: String, _: List<String> ->
            dummy.mockApplyFix(replacementRange, replacement)
        }

        dummy.applyFixes(fixFunction, fixes.replacementRanges, fixes.replacements, fixes.groups)

        assertEquals("This sentence contains one r and another r which are both the same.", dummy.dummyDocument)
    }

    /**
     * Test a case where nothing should be replaced.
     */
    @Test
    fun testApplyFixesNoReplacements() {
        val dummy = MockRegexInspection()
        dummy.dummyDocument = "This sentence contains nothing that should be replaced."

        val fixes = dummy.mockInspectFile()
        val fixFunction = { replacementRange: IntRange, replacement: String, _: List<String> ->
            dummy.mockApplyFix(replacementRange, replacement)
        }

        dummy.applyFixes(fixFunction, fixes.replacementRanges, fixes.replacements, fixes.groups)

        assertEquals("This sentence contains nothing that should be replaced.", dummy.dummyDocument)
    }

    /**
     * Test replacements which have the same length as the original.
     */
    @Test
    fun testApplyFixesSameLengthReplacements() {
        val dummy = MockRegexInspection()
        dummy.dummyDocument = "This sentence contains one blub and another blab which are both the same."

        val fixes = dummy.mockInspectFile("blob")
        val fixFunction = { replacementRange: IntRange, replacement: String, _: List<String> ->
            dummy.mockApplyFix(replacementRange, replacement)
        }

        dummy.applyFixes(fixFunction, fixes.replacementRanges, fixes.replacements, fixes.groups)

        assertEquals("This sentence contains one blob and another blob which are both the same.", dummy.dummyDocument)
    }
}