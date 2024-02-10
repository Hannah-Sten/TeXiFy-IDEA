package nl.hannahsten.texifyidea.action.preview

import com.intellij.openapi.project.Project

/**
 * @author Sergei Izmailov
 */
class PreviewFormUpdater(private val previewForm: PreviewForm) {

    /**
     * The default preamble.
     *
     * Unless you are going to set your own \pagestyle{}, simply append to this variable.
     */
    private val defaultPreamble =
        """
        \pagestyle{empty}
        """.trimIndent()

    /**
     * Preamble as specified by TeXiFy.
     * Only used if userPreamble is not empty.
     */
    var preamble = defaultPreamble

    /**
     * Preamble as specified by the user.
     * Modify this variable to include more packages.
     */
    var userPreamble = ""

    /**
     * Controls how long (in seconds) we will wait for the document compilation. If the time taken exceeds this,
     * we will return an error and not output a preview.
     */
    var waitTime = 3L

    /**
     * Reset the preamble to the default preamble.
     */
    fun resetPreamble() {
        preamble = defaultPreamble
        userPreamble = ""
    }

    /**
     * Sets the code that will be previewed, whether that be an equation, a tikz picture, or whatever else
     * you are trying to preview.
     *
     * This function also starts the creation and compilation of the temporary preview document, and will then
     * either display the preview, or if something failed, the error produced.
     */
    fun compilePreview(previewCode: String, project: Project, canUseJlatexmath: Boolean) {
        previewForm.setEquation(previewCode)

        // Combine default and user defined preamble. Cannot be used if we decide to run latexmath.
        val preamble = preamble + userPreamble

        // jlatexmath cannot handle a custom preamble
        val previewer = if (userPreamble.isBlank() && canUseJlatexmath) {
            JlatexmathPreviewer()
        }
        else {
            InkscapePreviewer()
        }
        previewer.preview(previewCode, previewForm, project, preamble, waitTime)
    }
}
