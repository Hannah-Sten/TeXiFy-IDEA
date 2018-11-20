package nl.rubensten.texifyidea.window

import javax.swing.*


class EquationPreviewToolWindow(val equation_text:String) {
    private val preview_form = PreviewForm()

    val content: JPanel
        get() = preview_form.panel!!

    val form:PreviewForm
        get() = preview_form
}
