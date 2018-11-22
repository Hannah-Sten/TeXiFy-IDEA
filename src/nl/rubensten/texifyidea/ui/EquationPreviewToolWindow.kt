package nl.rubensten.texifyidea.ui

import javax.swing.*


class EquationPreviewToolWindow(val equationText:String) {
    private val previewForm = PreviewForm()

    val content: JPanel
        get() = previewForm.panel!!

    val form:PreviewForm
        get() = previewForm
}
