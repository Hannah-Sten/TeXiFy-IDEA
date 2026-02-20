package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure
import nl.hannahsten.texifyidea.updateFilesets

class LatexFilesetTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "test/resources/fileset"

    private fun filesetOf(vararg files: PsiFile): Set<VirtualFile> = files.map { it.virtualFile }.toSet()

    fun testInputFile() {
        val main = myFixture.addFileToProject(
            "main.tex",
            """
                \documentclass{article}
                \input{a.tex}
            """.trimIndent()
        )
        val a = myFixture.addFileToProject(
            "a.tex",
            """
                \input{b}
            """.trimIndent()
        )
        val b = myFixture.addFileToProject(
            "b.tex",
            """
                \input{folder/c}
            """.trimIndent()
        )
        val c = myFixture.addFileToProject(
            "folder/c.tex",
            """
                Text
            """.trimIndent()
        )

        myFixture.updateFilesets()
        val fileset = LatexProjectStructure.getFilesets(myFixture.project)
        val expected = filesetOf(main, a, b, c)
        assertEquals(expected, fileset?.mapping?.keys)
    }

    fun testPackageInclusion() {
        val main = myFixture.addFileToProject(
            "main.tex",
            """
                \documentclass{article}
                \usepackage{a}
            """.trimIndent()
        )
        val a = myFixture.addFileToProject(
            "a.sty",
            """
                \ProvidesPackage{a}
                \RequirePackage{b}
            """.trimIndent()
        )
        val b = myFixture.addFileToProject(
            "b.sty",
            """
                \ProvidesPackage{b}
            """.trimIndent()
        )
        myFixture.updateFilesets()
        val fileset = LatexProjectStructure.getFilesets(myFixture.project)!!
        val expected = filesetOf(main, a, b)
        assertEquals(expected, fileset.mapping.keys)
        assertEquals(1, fileset.filesets.size)
        assertEquals(expected, fileset.filesets.first().files)
    }

    fun testImportPackage() {
        val main = myFixture.addFileToProject(
            "main.tex",
            """
                \documentclass{article}
                \usepackage{import}
                \begin{document}
                    \subimport{chapters/}{1}
                \end{document}
            """.trimIndent()
        )
        val one = myFixture.addFileToProject(
            "chapters/1.tex",
            """
                \chapter{One}
                \input{two}
            """.trimIndent()
        )
        val two = myFixture.addFileToProject(
            "chapters/two.tex",
            """
                \section{Two}
            """.trimIndent()
        )
        myFixture.updateFilesets()
        val fileset = LatexProjectStructure.getFilesets(myFixture.project)!!
        val expected = filesetOf(main, one, two)
        assertEquals(expected, fileset.mapping.keys)
        assertEquals(1, fileset.filesets.size)
        assertEquals(expected, fileset.filesets.first().files)
    }
}