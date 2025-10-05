package nl.hannahsten.texifyidea.util.labels

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * Finds all the defined bibtex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findBibtexLabelsInFileSetAsSequence(): Sequence<String> = findBibtexItems().asSequence()
    .mapNotNull {
        when (it) {
            is BibtexEntry -> it.name
            is LatexCommands -> it.requiredParameterText(0)
            else -> null
        }
    }

/**
 * Finds all specified bibtex entries
 */
fun PsiFile.findBibtexItems(): Collection<PsiElement> {
    // TODO: very inefficient, as it will search all bibtex entries in the project
    val fileset = LatexProjectStructure.getFilesetScopeFor(this)
    val allBibtex = NewBibtexEntryIndex.getAllKeys(fileset).flatMap {
        NewBibtexEntryIndex.getByName(it, fileset)
    }
    val bibitem = findBibitemCommands().toList()
    return (allBibtex + bibitem)
}

/**
 * Finds all \\bibitem-commands in the document
 */
fun PsiFile.findBibitemCommands(): Sequence<LatexCommands> = NewCommandsIndex.getByNameInFileSet("\\bibitem", this).asSequence()