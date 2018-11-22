package nl.rubensten.texifyidea.ui

import java.awt.image.BufferedImage
import javax.swing.*

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

    fun setEquation(equation: String){
        equationArea!!.text = equation
    }

    fun setPreview(image: BufferedImage, latexOutput: String){
        equationPanel!!.setImage(image)
        outputArea!!.text = latexOutput
    }

    fun setLatexErrorMessage(errorMessage: String){
        outputArea!!.text = errorMessage
        equationPanel!!.clearImage()
    }

    fun clear(){
        outputArea!!.text = ""
        equationArea!!.text = ""
        equationPanel!!.clearImage()
    }


}
