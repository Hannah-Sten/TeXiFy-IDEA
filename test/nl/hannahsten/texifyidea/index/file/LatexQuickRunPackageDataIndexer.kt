package nl.hannahsten.texifyidea.index.file

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File
import java.io.FileNotFoundException

/**
 * Run the package indexer on a local file.
 */
class LatexQuickRunPackageDataIndexer : BasePlatformTestCase() {
    fun testRun() {
        try {
            val text = File("/home/thomas/texlive/2020/texmf-dist/source/latex/base/doc.dtx").readText()
            val file = myFixture.configureByText("doc.dtx", text)
            val map = LatexExternalCommandDataIndexer().map(LatexExternalCommandDataIndexerTest.MockContent(file))
            println(map)
        }
        catch (e: FileNotFoundException) {
            println(e)
        }
    }
}