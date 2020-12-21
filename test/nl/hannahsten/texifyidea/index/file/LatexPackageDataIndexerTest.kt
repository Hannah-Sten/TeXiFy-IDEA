package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.file.LatexSourceFileType

class LatexPackageDataIndexerTest : BasePlatformTestCase() {

    fun testOneMacroOneLine() {
        val text = """
            %\begin{macro}{\gram}
            % The gram is an odd unit as it is needed for the base unit kilogram.
            %    \begin{macrocode}
            \DeclareSIUnit \gram { g }
            %    \end{macrocode}
            %\end{macro}
        """.trimIndent()
        val file = myFixture.configureByText("siunitx.dtx", text)
        val map = LatexPackageDataIndexer().map(MockContent(file))
        assertEquals("The gram is an odd unit as it is needed for the base unit kilogram.", map["\\gram"])
    }

    // todo more tests

    class MockContent(val file: PsiFile) : FileContent {
        override fun <T : Any?> getUserData(key: Key<T>): T? { return null }

        override fun <T : Any?> putUserData(key: Key<T>, value: T?) { }

        override fun getFileType() = LatexSourceFileType

        override fun getFile(): VirtualFile = file.virtualFile

        override fun getFileName() = "test"

        override fun getProject() = file.project

        override fun getContent() = ByteArray(0)

        override fun getContentAsText(): CharSequence = file.text

        override fun getPsiFile() = file

    }
}