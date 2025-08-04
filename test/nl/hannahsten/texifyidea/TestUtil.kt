package nl.hannahsten.texifyidea

import com.intellij.psi.PsiFile
import com.intellij.testFramework.common.timeoutRunBlocking

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import kotlin.time.Duration.Companion.seconds

fun CodeInsightTestFixture.updateFilesets() {
    timeoutRunBlocking(1.seconds) {
        LatexProjectStructure.updateFilesetsSuspend(project)
    }
}

fun CodeInsightTestFixture.configureByFilesAndBuildFilesets(vararg filenames: String): Array<out PsiFile> {
    return configureByFiles(*filenames).also {
        updateFilesets()
    }
}