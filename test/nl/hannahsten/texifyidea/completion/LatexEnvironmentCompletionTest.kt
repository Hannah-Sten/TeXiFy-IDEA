package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexEnvironmentCompletionTest : BasePlatformTestCase() {

    fun testEnvironmentCompletion() {
        myFixture.configureByText(LatexFileType, """\begin{cente<caret>}""")
        myFixture.checkResult("""\begin{center}""")
    }
}