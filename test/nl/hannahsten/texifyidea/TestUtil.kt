package nl.hannahsten.texifyidea

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import nl.hannahsten.texifyidea.index.LatexProjectStructure

fun CodeInsightTestFixture.updateFilesets() {
    LatexProjectStructure.testOnlyUpdateFilesets(project)
}

fun CodeInsightTestFixture.configureByFilesAndBuildFilesets(vararg filenames: String): Array<out PsiFile> {
    return configureByFiles(*filenames).also {
        updateFilesets()
    }
}