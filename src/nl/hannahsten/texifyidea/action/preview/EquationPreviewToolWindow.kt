package nl.hannahsten.texifyidea.action.preview

import nl.hannahsten.texifyidea.ui.PreviewForm
import javax.swing.JPanel

/**
 * @author Sergei Izmailov
 */
class EquationPreviewToolWindow {

    private val previewForm = PreviewForm()

    val content: JPanel
        get() = previewForm.panel!!

    val form: PreviewForm
        get() = previewForm
}
