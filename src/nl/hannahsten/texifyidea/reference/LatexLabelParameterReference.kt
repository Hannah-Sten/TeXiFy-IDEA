package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.files.findCommandInFileSet
import nl.hannahsten.texifyidea.util.labels.extractLabelElement
import nl.hannahsten.texifyidea.util.labels.extractLabelName
import nl.hannahsten.texifyidea.util.labels.findLatexLabelingElementsInFileSet

/**
 * The difference with [LatexLabelReference] is that this reference works on parameter text, i.e. the actual label parameters.
 * This means that the parameter of a \ref command will resolve to the parameter of the \label command.
 *
 * This allows us to implement find usages as well.
 *
 * @author Thomas
 */
class LatexLabelParameterReference(element: LatexParameterText) : PsiReferenceBase<LatexParameterText>(element), PsiPolyVariantReference {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return multiResolve(false).any { it.element == element }
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val externalDocumentCommand = myElement.containingFile.originalFile.findCommandInFileSet(LatexGenericRegularCommand.EXTERNALDOCUMENT)
        // Find the label definition
        val myElementName = myElement.name
        return myElement.containingFile.findLatexLabelingElementsInFileSet()
            .filter { it.extractLabelName(externalDocumentCommand) == myElementName }
            .toSet()
            .mapNotNull {
                // Find the normal text in the label command.
                // We cannot just resolve to the label command itself, because for Find Usages IJ will get the name of the element
                // under the cursor and use the words scanner to look for it (and then check if the elements found are references to the element under the cursor)
                // but only the label text itself will have the correct name for that.
                PsiElementResolveResult(it.extractLabelElement() ?: return@mapNotNull null)
            }
            .toTypedArray()
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}