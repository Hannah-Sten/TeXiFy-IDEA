package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.lang.Language
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.PsiFile
import com.intellij.psi.injection.Injectable
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.util.ui.EmptyIcon
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.MutableMagicComment
import nl.hannahsten.texifyidea.lang.magic.addMagicComment
import nl.hannahsten.texifyidea.lang.magic.magicComment
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.firstParentOfType
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.SwingConstants

/**
 * @author Sten Wessel
 */
class LatexLanguageInjectionIntention : TexifyIntentionBase("Inject language") {

    override fun getFamilyName() = "Inject language"

    override fun getText() = "Permanently inject language in environment"

    override fun startInWriteAction() = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file !is LatexFile || editor == null) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false

        val env = element.firstParentOfType(LatexEnvironment::class) ?: return false

        return !env.magicComment().containsKey(DefaultMagicKeys.INJECT_LANGUAGE)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (file !is LatexFile || editor == null) {
            return
        }

        chooseLanguage(editor) { language ->
            WriteCommandAction.runWriteCommandAction(project) {
                inject(file, editor, language)
            }
        }
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        // Choose random language for the preview, as we cannot show a popup
        val language = injectableLanguages().firstOrNull() ?: return IntentionPreviewInfo.EMPTY
        inject(file, editor, language)
        return IntentionPreviewInfo.DIFF
    }

    private fun inject(
        file: PsiFile,
        editor: Editor,
        language: Injectable
    ) {
        val element = file.findElementAt(editor.caretModel.offset)
        val comment = MutableMagicComment<String, String>().apply { addValue(DefaultMagicKeys.INJECT_LANGUAGE, language.id) }
        element?.firstParentOfType(LatexEnvironment::class)?.addMagicComment(comment)
    }

    private fun chooseLanguage(editor: Editor, onChosen: (language: Injectable) -> Unit) {
        // Dummy to determine height of single cell
        val dimension = JLabel(LatexLanguage.displayName, EmptyIcon.ICON_16, SwingConstants.LEFT).minimumSize
        dimension.height *= 4

        val list = JBList(injectableLanguages()).apply {
            cellRenderer = object : ColoredListCellRenderer<Injectable>() {
                override fun customizeCellRenderer(list: JList<out Injectable>, language: Injectable, index: Int, selected: Boolean, hasFixed: Boolean) {
                    icon = language.icon
                    append(language.displayName)
                }
            }

            minimumSize = dimension
        }

        val popup = PopupChooserBuilder(list).setItemChoosenCallback { onChosen(list.selectedValue ?: return@setItemChoosenCallback) }
            .setFilteringEnabled { language -> (language as Injectable).displayName }
            .setMinSize(dimension)
            .createPopup()

        popup.showInBestPositionFor(editor)
    }

    private fun injectableLanguages(): List<Injectable> = Language.getRegisteredLanguages().map { Injectable.fromLanguage(it) }
}
