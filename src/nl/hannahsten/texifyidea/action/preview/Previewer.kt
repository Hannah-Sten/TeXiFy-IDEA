package nl.hannahsten.texifyidea.action.preview

import com.intellij.openapi.project.Project

/**
 * Can compile LaTeX into a preview image.
 */
interface Previewer {

    /**
     * Given the LaTeX input, construct an image and update the form, with image, output or error message.
     */
    fun preview(input: String, previewForm: PreviewForm, project: Project, preamble: String, waitTime: Long)
}