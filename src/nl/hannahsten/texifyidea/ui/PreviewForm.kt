package nl.hannahsten.texifyidea.ui

import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTextArea

/**
 * @author Sergei Izmailov
 */
class PreviewForm {

    var panel: JPanel? = null
    private var equationArea: JTextArea? = null
    private var outputArea: JTextArea? = null
    private var equationPanel: ImagePanel? = null
    private var latexOutputTab: JPanel? = null
    private var equationTab: JPanel? = null
    private var tabbedPane: JTabbedPane? = null

    fun setEquation(equation: String) {
        equationArea!!.text = equation
    }

    fun setPreview(image: BufferedImage) {
        equationPanel!!.setImage(image)
        tabbedPane!!.selectedIndex = tabbedPane!!.indexOfComponent(equationTab)
    }

    fun setLatexErrorMessage(errorMessage: String) {
        outputArea!!.text = errorMessage
        equationPanel!!.clearImage()
        tabbedPane!!.selectedIndex = tabbedPane!!.indexOfComponent(latexOutputTab)
    }

    fun clear() {
        outputArea!!.text = ""
        equationArea!!.text = ""
        equationPanel!!.clearImage()
    }
}
