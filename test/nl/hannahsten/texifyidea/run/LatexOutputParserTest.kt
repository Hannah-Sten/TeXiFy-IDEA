package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.Assert
import nl.hannahsten.texifyidea.run.latex.ui.LatexCompileMessageTreeView
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener.LatexLogMessageType.ERROR
import javax.swing.DefaultListModel

class LatexOutputParserTest : BasePlatformTestCase() {
    fun testErrorSimpleFile() {
        val text = "./main.tex:1: LaTeX Error: Environment align undefined.\n"
        val listModel = DefaultListModel<String>()
        val treeView = LatexCompileMessageTreeView(project)

        val listener = LatexOutputListener(project, null, listModel, treeView)
        val (message, fileName, line, type) = listener.findMessage(text, "\n")
                ?: return Assert.fail()

        assertEquals("LaTeX Error: Environment align undefined.", message)
        assertEquals("./main.tex", fileName)
        assertEquals(0, line)
        assertEquals(ERROR, type)
    }
}