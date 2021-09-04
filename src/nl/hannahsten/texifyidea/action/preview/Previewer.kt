package nl.hannahsten.texifyidea.action.preview

import nl.hannahsten.texifyidea.ui.PreviewForm

/**
 * Can compile LaTeX into a preview image.
 */
interface Previewer {

    /**
     * Given the LaTeX input, construct an image and update the form, with image, output or error message.
     */
    fun preview(input: String, previewForm: PreviewForm)
}