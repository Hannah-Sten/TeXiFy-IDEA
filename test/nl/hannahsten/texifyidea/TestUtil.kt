package nl.hannahsten.texifyidea

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import junit.framework.TestCase.assertEquals
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure
import kotlin.time.Duration.Companion.seconds

fun CodeInsightTestFixture.updateFilesets() {
    timeoutRunBlocking(10.seconds) {
        LatexProjectStructure.updateFilesetsSuspend(project)
    }
}

fun CodeInsightTestFixture.updateCommandDef() {
    timeoutRunBlocking(10.seconds) {
        LatexDefinitionService.getInstance(project).ensureRefreshAll()
    }
}

fun CodeInsightTestFixture.configureByFilesAndBuildFilesets(vararg filenames: String): Array<out PsiFile> = configureByFiles(*filenames).also {
    updateFilesets()
}

fun CodeInsightTestFixture.testFoldingWithDefinitions(content: String, fileName: String = "test.tex") {
    val expectedText = content.trimIndent()
    val rawText = CodeInsightTestFixtureImpl.removeFoldingMarkers(expectedText)
    configureByText(fileName, rawText)
    updateCommandDef()

    val actual = (this as CodeInsightTestFixtureImpl).getFoldingDescription(false)
    assertEquals(
        "Folding mismatch for inline content in $fileName",
        StringUtil.convertLineSeparators(expectedText),
        StringUtil.convertLineSeparators(actual)
    )
}
