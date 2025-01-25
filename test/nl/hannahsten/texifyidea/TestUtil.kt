package nl.hannahsten.texifyidea

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetService
import nl.hannahsten.texifyidea.util.files.psiFile


fun CodeInsightTestFixture.configureByFilesWithMockCache(vararg filenames: String) {
    val files = filenames.mapNotNull { copyFileToProject(it).psiFile(project) }
    val mockService = mockk<ReferencedFileSetService>()
    every { mockService.referencedFileSetOf(any()) } returns files.toSet()
    every { mockService.rootFilesOf(any()) } returns setOf(files.first())
    every { mockService.dropCaches(any()) } answers { callOriginal() }
    every { mockService.dropCaches(any()) } answers { callOriginal() }
    every { mockService.dropAllCaches() } answers { callOriginal() }
    every { mockService.markCacheOutOfDate() } answers { callOriginal() }
    mockkObject(ReferencedFileSetService.Companion)
    every { ReferencedFileSetService.getInstance() } returns mockService
    openFileInEditor(files.first().virtualFile)
}