package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiFile
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.completion.handlers.TokenTypeInsertHandler
import nl.rubensten.texifyidea.lang.BibtexDefaultEntry
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.psi.BibtexKey
import nl.rubensten.texifyidea.util.*

/**
 * @author Ruben Schellekens
 */
object BibtexKeyProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        val psiElement = parameters.position
        val entry = psiElement.parentOfType(BibtexEntry::class) ?: return
        val token = entry.tokenType() ?: return
        val entryType = BibtexDefaultEntry[token] ?: return
        val optional = entryType.optional.map { it.fieldName }.toSet()
        val required = entryType.required.map { it.fieldName }.toSet()

        // Removed already present items.
        val allFields = entryType.allFields().map { it.fieldName }
        val userDefined = findUserDefinedKeys(entry.containingFile, allFields)
        val fields = allFields + userDefined
        val keys = entry.keyNames()
        val notPresent = fields - keys

        // Add lookup elements.
        result.addAllElements(ContainerUtil.map2List(notPresent, {
            val (message, icon) = when (it) {
                in required -> " required" and TexifyIcons.KEY_REQUIRED
                in optional -> " optional" and PlatformIcons.PROTECTED_ICON
                in userDefined -> " custom" and TexifyIcons.KEY_USER_DEFINED
                else -> "" and PlatformIcons.PROTECTED_ICON
            }

            LookupElementBuilder.create(it, it)
                    .withPresentableText(it)
                    .bold()
                    .withTypeText(message, true)
                    .withIcon(icon)
                    .withInsertHandler(TokenTypeInsertHandler)
        }))
    }

    /**
     * Scans the given file for fields that are defined by the user.
     *
     * @param file
     *          The file to scan.
     * @param allFields
     *          All fields that are already in the autocomplete.
     * @return A list containing all user defined keys.
     */
    private fun findUserDefinedKeys(file: PsiFile, allFields: List<String>): Set<String> {
        val result = HashSet<String>()
        val presentFieldSet = allFields.toMutableSet()

        for (key in file.childrenOfType(BibtexKey::class)) {
            val name = key.text
            if (name !in presentFieldSet) {
                presentFieldSet.add(name)
                result.add(name)
            }
        }

        return result
    }
}