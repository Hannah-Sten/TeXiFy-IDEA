package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiFile
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.TokenTypeInsertHandler
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexKey
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.parentOfType

/**
 * @author Hannah Schellekens
 */
object BibtexKeyProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val psiElement = parameters.position
        val entry = psiElement.parentOfType(BibtexEntry::class) ?: return
        val token = entry.tokenType()
        val entryType = BibtexDefaultEntryType[token] ?: return
        val optional: Set<BibtexEntryField> = entryType.optional.toSet()
        val required: Set<BibtexEntryField> = entryType.required.toSet()

        // Removed already present items.
        val allFields: Set<BibtexEntryField> = entryType.allFields().toSet()
        val userDefined: Set<BibtexEntryField> = findUserDefinedKeys(entry.containingFile, allFields)
        val keys = entry.keyNames()
        val fields = (allFields + userDefined).toMutableSet()
        fields.removeIf { it.fieldName in keys }

        // Add lookup elements.
        result.addAllElements(
            ContainerUtil.map2List(fields) {
                val (message, icon) = when (it) {
                    in required -> " required" and TexifyIcons.KEY_REQUIRED
                    in optional -> " optional" and TexifyIcons.KEY_USER_DEFINED
                    in userDefined -> " custom" and TexifyIcons.KEY_USER_DEFINED
                    else -> "" and TexifyIcons.KEY_USER_DEFINED
                }

                LookupElementBuilder.create(it, it.fieldName)
                    .withPresentableText(it.fieldName)
                    .bold()
                    .withTypeText(message, true)
                    .withIcon(icon)
                    .withTailText(packageName(it), true)
                    .withInsertHandler(TokenTypeInsertHandler)
            }
        )
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
    private fun findUserDefinedKeys(file: PsiFile, allFields: Collection<BibtexEntryField>): Set<BibtexEntryField> {
        val result = HashSet<BibtexEntryField>()
        val presentFieldSet: MutableSet<String> = allFields.map { it.fieldName }
            .toMutableSet()

        for (key in file.childrenOfType(BibtexKey::class)) {
            val name = key.text
            if (name !in presentFieldSet) {
                presentFieldSet.add(name)
                result.add(SimpleBibtexEntryField(name, "User defined string."))
            }
        }

        return result
    }

    private fun packageName(dependend: Dependend): String {
        return when (val dependency = dependend.dependency) {
            LatexPackage.DEFAULT -> ""
            else -> "  (${dependency.name})"
        }
    }
}