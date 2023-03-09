package nl.hannahsten.texifyidea.highlighting

import com.intellij.codeInsight.daemon.RainbowVisitor
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.definedCommandName
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.isCommandDefinition

/**
 * Semantic highlighting.
 * See https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html#semantic-highlighting
 */
class LatexRainbowVisitor : RainbowVisitor() {
    override fun suitableForFile(file: PsiFile) = file.isLatexFile()

    override fun visit(element: PsiElement) {
        if (element !is LatexCommands) return

        // We use semantic highlighting for user-defined commands, because the normal LatexSyntaxHighlighter can only use token type,
        // not the index (it would be too slow anyway), and the parser cannot know which commands are user defined.
        val allUserCommands = LatexDefinitionIndex.getItems(element.project)
            .filter{ it.isCommandDefinition() }
            .map { it.definedCommandName() }

        if (element.name in allUserCommands) {
            val globalScheme = EditorColorsManager.getInstance().globalScheme
            val key = LatexSyntaxHighlighter.USER_DEFINED_COMMAND_KEY
            val color = globalScheme.getAttributes(key)?.foregroundColor
            // todo not sure why we need to provide a "name" or what it means
            addInfo(getInfo(element, element.commandToken, color.toString(), key))
        }
    }

    override fun clone(): HighlightVisitor {
        return LatexRainbowVisitor()
    }
}