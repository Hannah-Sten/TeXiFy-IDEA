package nl.hannahsten.texifyidea

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import io.mockk.every
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.util.files.psiFile

fun CodeInsightTestFixture.configureByFilesWithMockCache(vararg filenames: String) {
    val files = filenames.mapNotNull { copyFileToProject(it).psiFile(project) }
//    val files = configureByFiles(*filenames)
    mockkObject(LatexProjectStructure)
    every { LatexProjectStructure.getFilesetScopeFor(any(), any()) } returns GlobalSearchScope.filesScope(project, files.map { it.virtualFile })
    openFileInEditor(files.first().virtualFile)
}