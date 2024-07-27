package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class RightInsertHandlerTest : BasePlatformTestCase() {
    fun testLeftRightBraces() {
        myFixture.configureByText(LatexFileType, """$\lef<caret> $""")
        myFixture.complete(CompletionType.BASIC)
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        myFixture.checkResult("""$\left( <caret> \right) $""")
    }

    fun testLeftRightMatchingBraces() {
        myFixture.configureByText(LatexFileType, """$\lef<caret> )$""")
        myFixture.complete(CompletionType.BASIC)
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        myFixture.checkResult("""$\left( <caret> )$""")
    }
}