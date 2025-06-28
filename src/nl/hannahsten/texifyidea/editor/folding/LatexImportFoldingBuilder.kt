package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.allCommands
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.parentOfType

/**
 * Folds multiple \\usepackage or \\RequirePackage statements.
 * This is not DumbAware.
 *
 * @author Hannah Schellekens
 */
open class LatexImportFoldingBuilder : FoldingBuilderEx() {

    private val includesSet = setOf(LatexGenericRegularCommand.USEPACKAGE.cmd, LatexGenericRegularCommand.REQUIREPACKAGE.cmd)

    override fun isCollapsedByDefault(node: ASTNode) = LatexCodeFoldingSettings.getInstance().collapseImports

    override fun getPlaceholderText(node: ASTNode) = if (node.text.contains("RequirePackage")) "\\RequirePackage{...}" else "\\usepackage{...}"

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val covered = HashSet<LatexCommands>()
        val commands = root.allCommands().filter { it.name in includesSet }

        for (command in commands) {
            // Do not cover commands twice.
            if (command in covered) {
                continue
            }

            // Iterate over all consecutive commands.
            var next: LatexCommands? = command
            var last: LatexCommands = command
            while (next != null && next.name in includesSet) {
                covered += next
                last = next
                next = next.nextCommand()
            }

            descriptors.add(FoldingDescriptor(command, TextRange(command.startOffset, last.endOffset)))
        }

        return descriptors.toTypedArray()
    }

    private fun PsiElement.nextCommand(): LatexCommands? {
        val content = if (this is LatexCommands) {
            parentOfType(LatexNoMathContent::class) ?: return null
        }
        else this
        val next = content.nextSibling

        // When having multiple breaks, don't find new commands to fold.
        // When whitespace without multiple breaks, look further.
        if (next is PsiWhiteSpace) {
            return if (PatternMagic.containsMultipleNewlines.matcher(next.text).matches()) {
                null
            }
            else next.nextCommand()
        }

        // No next? Then there is no valid next command. This happens e.g. when it's the last element of the file.
        next ?: return null

        // Skip comments.
        if (next is PsiComment) {
            return next.nextCommand()
        }

        return next.findFirstChildOfType(LatexCommands::class)
    }
}