package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.file.LatexSourceFileType

class LatexExternalEnvironmentDataIndexerTest : BasePlatformTestCase() {

    fun testDescribeEnv() {
        val text = """
            % different index entry. Below I used |\DescribeEnv{verbatim}|.
            %
            % \DescribeEnv{verbatim}
            % It is often a good idea to include examples of the usage of new macros
            % in the text. Because of the |%| sign in the first column of every
            % row, the \textsf{verbatim} environment is slightly altered to suppress
            % those
            % characters.\footnote{These macros were written by Rainer
            %                      Sch\"opf~\cite{art:verbatim}. He also
            %                      provided a new \textsf{verbatim} environment
            %                      which can be used inside of other macros.}
            % 
        """.trimIndent()
        val file = myFixture.configureByText("doc.dtx", text)
        val map = LatexExternalEnvironmentDataIndexer().map(MockContent(file))
        assertEquals("It is often a good idea to include examples of the usage of new macros<br> in the text. Because of the % sign in the first column of every<br> row, the verbatim environment is slightly altered to suppress<br> those<br> characters.", map["verbatim"])
    }

    fun testNewEnvironment() {
        val text = """
%
%
% \subsection{Macros surrounding the `definition parts'}
%
% \begin{environment}{macrocode}
%    Parts of the macro definition will be surrounded by the
%    environment \textsf{macrocode}.  Put more precisely, they will be
%    enclosed by a macro whose argument (the text to be set
%    `verbatim') is terminated by the string
% \verb*+%    \end{macrocode}+.  Carefully note the number of spaces.
% \changes{v1.5r}{1989/11/04}{Support for code line no. (Undoc)}
%    \begin{macrocode}
\def\macrocode{\macro@code
...
%    \end{macrocode}
% \end{environment}
%
%
        """.trimIndent()
        val file = myFixture.configureByText("doc.dtx", text)
        val map = LatexExternalEnvironmentDataIndexer().map(MockContent(file))
        assertEquals("Parts of the macro definition will be surrounded by the<br> environment macrocode.  Put more precisely, they will be<br> enclosed by a macro whose argument (the text to be set<br> `verbatim') is terminated by the string<br> \\verb*+%    \\end{macrocode}+.  Carefully note the number of spaces.", map["macrocode"])
    }

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