package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ex.ProblemDescriptorImpl
import com.intellij.mock.MockProject
import com.intellij.mock.MockProjectEx
import com.intellij.mock.MockPsiElement
import com.intellij.mock.MockPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.MockProblemDescriptor
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import nl.rubensten.texifyidea.psi.BibtexQuotedString
import nl.rubensten.texifyidea.psi.LatexMathContent
import nl.rubensten.texifyidea.util.length
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class TexifyRegexInspectionTest : LightCodeInsightFixtureTestCase() {

    class DummyRegexInspection : TexifyRegexInspection(
            inspectionDisplayName = "",
            myInspectionId = "",
            errorMessage = { "" },
            pattern = Pattern.compile("aaa.aaa"),
            mathMode = true,
            replacement = { _, _ -> "The replacement" },
            replacementRange = { IntRange(24, 42) },
            quickFixName = { "Insert amssymb symbol." }) {

        // Provide dummy document contents
        val dummyDocument = "The words aaabaaa and aaacaaa should both be replaced in the correct location."

        // Remove the first part of the applyfix functionality to avoid trying to replace things in a non-existing file
        override fun applyFix(descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
            dummyDocument.replaceRange(replacementRange.start, replacementRange.endInclusive, replacement)

            return replacement.length - replacementRange.length
        }
    }
//
//    class MyProjectDescriptor : LightProjectDescriptor() {
//
//    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return DefaultLightProjectDescriptor()
    }

    @Test
    fun testApplyFixesTwoReplacements() {
        val dummy = DummyRegexInspection()
        val replacementRanges = arrayListOf(IntRange(10, 17), IntRange(22, 28))
        val replacements = arrayListOf("Replacement1", "Replacement2")
        val groups = arrayListOf<List<String>>(arrayListOf())

//        val dummyProject = MockProject()
//        val dummyPsiElement = MockPsiElement("eh?", dummyProject)
        val dummyPsiElement = MockPsiFile(PsiManager.getInstance(MockProjectEx {}))
        val dummyProblemDescriptor = MockProblemDescriptor(dummyPsiElement, "", ProblemHighlightType.ERROR)
        dummy.applyFixes(dummyProblemDescriptor, replacementRanges, replacements, groups)

        assertEquals("The words Replacement1 and Replacement2 should both be replaced in the correct location.", dummy.dummyDocument)
    }
}