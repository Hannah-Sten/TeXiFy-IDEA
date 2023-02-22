package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.file.BibtexFileType
import kotlin.test.assertContentEquals

class BibtexExternalEntryDataIndexerTest : BasePlatformTestCase() {
    fun testSingleEntry() {
        val text = """
            @book{Parker2020,
            	title = {Humble Pi},
            	author = {Parker, Matt},
            	year = {2020}
            }
        """.trimIndent()

        val file = myFixture.configureByText("test.bib", text)
        val map = BibtexExternalEntryDataIndexer.map(MockContent(file))
        assertContentEquals(listOf("Parker2020"), map.keys)
    }

    class MockContent(val file: PsiFile) : FileContent {

        override fun <T : Any?> getUserData(key: Key<T>): T? { return null }

        override fun <T : Any?> putUserData(key: Key<T>, value: T?) { }

        override fun getFileType() = BibtexFileType

        override fun getFile(): VirtualFile = file.virtualFile

        override fun getFileName() = "test"

        override fun getProject() = file.project

        override fun getContent() = file.text.toByteArray()

        override fun getContentAsText(): CharSequence = file.text

        override fun getPsiFile() = file
    }
}