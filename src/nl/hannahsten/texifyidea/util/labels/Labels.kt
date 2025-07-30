package nl.hannahsten.texifyidea.util.labels

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * All labels in the fileset.
 * May contain duplicates.
 */
fun PsiFile.findLatexLabelingElementsInFileSet(): Sequence<PsiElement> {
    // TODO: Better implementation
    val fileset = LatexProjectStructure.getFilesetScopeFor(this)
    return NewLabelsIndex.getAllLabels(fileset).asSequence().flatMap {
        NewLabelsIndex.getByName(it, fileset)
    }
}

/*
 * Filtering sequence or collection
 */

object Labels {

    fun isDefinedLabelOrBibtexLabel(label: String, project: Project, scope: GlobalSearchScope): Boolean {
        return NewLabelsIndex.existsByName(label, project, scope) || NewBibtexEntryIndex.existsByName(label, project, scope)
    }

    fun getUniqueLabelName(originalLabel: String, file: PsiFile): String {
        val project = file.project
        val fileset = LatexProjectStructure.getFilesetScopeFor(file)
        var counter = 2
        var candidate = originalLabel
        while(isDefinedLabelOrBibtexLabel(candidate, project, fileset)) {
            candidate = originalLabel + counter
            counter++
        }
        return candidate
    }
}