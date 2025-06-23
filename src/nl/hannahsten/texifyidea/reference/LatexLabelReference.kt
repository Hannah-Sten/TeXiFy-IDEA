package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.findCommandInFileSet
import nl.hannahsten.texifyidea.util.files.findExternalDocumentCommand
import nl.hannahsten.texifyidea.util.labels.extractLabelName
import nl.hannahsten.texifyidea.util.labels.findBibtexItems
import nl.hannahsten.texifyidea.util.labels.findLatexLabelingElementsInFileSet
import nl.hannahsten.texifyidea.util.labels.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import java.util.*

/**
 * A reference to a label, used only for autocompletion (together with LatexCommandsImplUtil#extractLabelReferences). For the real referencing, see [LatexLabelParameterReference].
 *
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexLabelReference(element: LatexCommands, range: TextRange?) : PsiReferenceBase<LatexCommands?>(element) {

    override fun resolve(): PsiElement? {
        return null
    }

    override fun getVariants(): Array<Any> {
        val file = myElement!!.containingFile.originalFile
        val command = (myElement as LatexCommands).commandToken.text

        // add bibreferences to autocompletion for \cite-style commands
        if (CommandMagic.bibliographyReference.contains(command)) {
            return file.findBibtexItems().stream()
                .map { bibtexEntry: PsiElement? ->
                    if (bibtexEntry != null) {
                        val containing = bibtexEntry.containingFile
                        if (bibtexEntry is LatexCommands) {
                            val parameters = bibtexEntry.getRequiredParameters()
                            return@map LookupElementBuilder.create(parameters[0])
                                .bold()
                                .withInsertHandler(LatexReferenceInsertHandler())
                                .withTypeText(
                                    containing.name + ": " +
                                        (
                                            1 + StringUtil.offsetToLineNumber(
                                                containing.text,
                                                bibtexEntry.textOffset
                                            )
                                            ),
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
            // Create autocompletion entries for each element we could possibly resolve to
            val externalDocumentCommand = file.findExternalDocumentCommand()
            return file.findLatexLabelingElementsInFileSet()
                .toSet()
                .mapNotNull { labelingElement: PsiElement ->
                    val extractedLabel = labelingElement.extractLabelName(externalDocumentCommand)
                    if (extractedLabel.isBlank()) return@mapNotNull null

                    LookupElementBuilder
                        .create(extractedLabel)
                        .bold()
                        .withInsertHandler(LatexReferenceInsertHandler())
                        .withTypeText(
                            labelingElement.containingFile.name + ":" +
                                (
                                    1 + StringUtil.offsetToLineNumber(
                                        labelingElement.containingFile.text,
                                        labelingElement.textOffset
                                    )
                                    ),
                            true
                        )
                        .withIcon(TexifyIcons.DOT_LABEL)
                }.toTypedArray()
        }
        // if command isn't ref or cite-styled return empty array
        return arrayOf()
    }

    init {

        // Only show Ctrl+click underline under the reference name
        if (range != null) {
            rangeInElement = range
        }
    }
}