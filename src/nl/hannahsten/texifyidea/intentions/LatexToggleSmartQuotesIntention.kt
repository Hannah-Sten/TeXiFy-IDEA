package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.settings.TexifySettings
import kotlin.math.max

/**
 * @author Hannah Schellekens
 */
open class LatexToggleSmartQuotesIntention : TexifyIntentionBase("Toggle smart quotes") {

    companion object {

        private val triggerCharacters = setOf('\'', '"', '`')
    }

    /**
     * Contains the preferred setting of the user if they have already selected one.
     */
    private var selectedSmartQuoteSetting = when (val replacement = TexifySettings.getInstance().automaticQuoteReplacement) {
        TexifySettings.QuoteReplacement.NONE -> TexifySettings.QuoteReplacement.LIGATURES
        else -> replacement
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        val caret = editor?.caretModel ?: return false
        val element = file?.findElementAt(caret.offset) ?: return false

        // Also check the element 1 position back, because the following line:
        // ``kameel''
        // with the cursor at the end will result in the following whitespace (therefore, no quotes).
        val elementBefore = file.findElementAt(max(0, caret.offset - 1)) ?: return false

        return triggerCharacters.any { char ->
            element.textContains(char) || elementBefore.textContains(char)
        }
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val settings = TexifySettings.getInstance()
        val result = when (settings.automaticQuoteReplacement) {
            TexifySettings.QuoteReplacement.NONE -> selectedSmartQuoteSetting
            else -> TexifySettings.QuoteReplacement.NONE
        }

        settings.automaticQuoteReplacement = result
    }
}