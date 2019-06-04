package nl.rubensten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexIncludesIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.includedFileNames
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Sten Wessel
 */
open class BibtexDuplicateBibliographyInspection : TexifyInspectionBase() {

    // Manual override to match short name in plugin.xml
    override fun getShortName() = InsightGroup.BIBTEX.prefix + inspectionId

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "DuplicateBibliography"

    override fun getDisplayName() = "Same bibliography is included multiple times"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val groupedIncludes = mutableMapOf<String, MutableList<LatexCommands>>()

        LatexIncludesIndex.getItemsInFileSet(file).asSequence()
                .filter { it.name == "\\bibliography" || it.name == "\\addbibresource" }
                .forEach {
                    for (fileName in it.includedFileNames() ?: emptyList()) {
                        groupedIncludes.getOrPut(fileName) { mutableListOf() }.add(it)
                    }
                }

        groupedIncludes.asSequence()
                .filter { it.value.size > 1 }
                .forEach {
                    val fileName = it.key
                    val commands = it.value.distinct()
                    for (command in commands) {
                        descriptors.add(manager.createProblemDescriptor(
                                command,
                                TextRange(0, command.requiredParameter(0)?.length!!).shiftRight(command.commandToken.textLength + 1),
                                "Bibliography file '$fileName' is included multiple times",
                                ProblemHighlightType.GENERIC_ERROR,
                                isOntheFly
                        ))
                    }
                }

        return descriptors
    }
}
