package nl.hannahsten.texifyidea.index.file

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.FileNotFoundException

/**
 * Run the package indexer on a local file.
 */
class LatexQuickRunPackageDataIndexer : BasePlatformTestCase() {

    fun testRun() {
        try {
//            val filesToIndex = mutableSetOf<VirtualFile>()
//            LatexSdkUtil.getSdkSourceRoots(project).forEach { root ->
//                filesToIndex.addAll(
//                    root.allChildFiles().filter { it.extension == LatexSourceFileType.defaultExtension })
//            }
//            val text = File("/home/thomas/texlive/2020/texmf-dist/source/latex/base/doc.dtx").readText()
//            val file = myFixture.configureByText("doc.dtx", text)
//            val map = LatexExternalCommandDataIndexer().map(LatexExternalCommandDataIndexerTest.MockContent(file))
//            filesToIndex.forEach {
//                val map = LatexExternalCommandDataIndexer().map(LatexExternalCommandDataIndexerTest.MockContent(it.psiFile(myFixture.project) ?: return@forEach))
//                println(map)
//            }
        }
        catch (e: FileNotFoundException) {
            println(e)
        }
    }
}