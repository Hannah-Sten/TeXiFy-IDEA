package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.labels.extractLabelName
import nl.hannahsten.texifyidea.util.labels.findBibtexItems
import nl.hannahsten.texifyidea.util.labels.findLatexLabelingElementsInFileSet
import nl.hannahsten.texifyidea.util.labels.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import java.util.*

/**
 * A reference to a label, used only for autocompletion. For the real referencing, see [LatexLabelParameterReference].
 *
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexLabelReference(element: LatexCommands, range: TextRange?) : PsiReferenceBase<LatexCommands?>(element) {

    override fun resolve(): PsiElement? {
        return null
    }

    override fun getVariants(): Array<Any> {
        val file = myElement!!.containingFile.originalFile
        val command = myElement.commandToken.text

        // add bibreferences to autocompletion for \cite-style commands
        if (CommandMagic.bibliographyReference.contains(command)) {
            return file.findBibtexItems().stream()
                .map { bibtexEntry: PsiElement? ->
                    if (bibtexEntry != null) {
                        val containing = bibtexEntry.containingFile
                        if (bibtexEntry is LatexCommands) {
                            val parameters = bibtexEntry.requiredParameters
                            return@map LookupElementBuilder.create(parameters[0])
                                .bold()
                                .withInsertHandler(LatexReferenceInsertHandler())
                                .withTypeText(
                                    containing.name + ": " +
                                            (1 + StringUtil.offsetToLineNumber(
                                                containing.text,
                                                bibtexEntry.getTextOffset()
                                            )),
                                    true
                                )
                                .withIcon(TexifyIcons.DOT_BIB)
                        }
                        else {
                            return@map null
                        }
                    }
                    null
                }.filter { o: LookupElementBuilder? -> Objects.nonNull(o) }.toArray()
        }
        else if (element.project.getLabelReferenceCommands().contains(command)) {
            return file.findLatexLabelingElementsInFileSet()
                .filter { it.extractLabelName().isNotBlank() }
                .map { labelingCommand: PsiElement ->
                    LookupElementBuilder
                        .create(labelingCommand.extractLabelName())
                        .bold()
                        .withInsertHandler(LatexReferenceInsertHandler())
                        .withTypeText(
                            labelingCommand.containingFile.name + ":" +
                                    (
                                            1 + StringUtil.offsetToLineNumber(
                                                labelingCommand.containingFile.text,
                                                labelingCommand.textOffset
                                            )
                                            ),
                            true
                        )
                        .withIcon(TexifyIcons.DOT_LABEL)
                }.toList().toTypedArray()
        }
        // if command isn't ref or cite-styled return empty array
        return arrayOf()
    }

    init {

        // Only show Ctrl+click underline under the reference name
        setRangeInElement(range)
    }
}