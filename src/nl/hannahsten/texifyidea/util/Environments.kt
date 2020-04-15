package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.LatexAnnotation
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexEnvironment

/**
 * Finds all environment definition commands within the project.
 *
 * @return The found definition commands.
 */
fun Project.findEnvironmentDefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this).filter {
        it.name in Magic.Command.environmentDefinitions
    }
}

fun LatexEnvironment.annotations(): List<LatexAnnotation> {
    val result = emptyList<LatexAnnotation>().toMutableList()

    var prev = this.parentOfType(LatexContent::class)?.previousSiblingIgnoreWhitespace()
    while (prev is PsiComment) {
        val annotation = LatexAnnotation.fromComment(prev) ?: return result
        result.add(annotation)

        prev = prev.previousSiblingIgnoreWhitespace()
    }

    return result
}