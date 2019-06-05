package nl.rubensten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexIncludesIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.*

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
                        if (command.containingFile != file) continue

                        descriptors.add(manager.createProblemDescriptor(
                                command,
                                TextRange(0, command.requiredParameter(0)?.length!!).shiftRight(command.commandToken.textLength + 1),
                                "Bibliography file '$fileName' is included multiple times",
                                ProblemHighlightType.GENERIC_ERROR,
                                isOntheFly,
                                RemoveOtherCommandsFix(fileName, commands)
                        ))
                    }
                }

        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    class RemoveOtherCommandsFix(private val bibName: String, private val commandsToFix: List<LatexCommands>) :
            LocalQuickFix {

        override fun getFamilyName(): String {
            return "Remove other includes of $bibName"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val currentCommand = descriptor.psiElement as LatexCommands
            val documentManager = PsiDocumentManager.getInstance(project)

            // For all commands to be fixed, remove the matching bibName
            // Handle commands by descending offset, to make sure the replaceString calls work correctly
            for (command in commandsToFix.sortedByDescending { it.textOffset }) {
                val document = command.containingFile.document() ?: continue
                val param = command.requiredParameters().first()

                // If we handle the current command, find the first occurrence of bibName and leave it intact
                val firstBibIndex = if (command == currentCommand) {
                    param.text.splitToSequence(',').indexOfFirst { it.matchesBibName() }
                } else -1

                val replacement = param.text.trimRange(1, 1).splitToSequence(',')
                        .filterIndexed { i, it -> i <= firstBibIndex || it.matchesBibName() }
                        .joinToString(",", prefix = "{", postfix = "}")

                // When no arguments are left, just delete the command
                if (replacement.trimRange(1, 1).trim().isEmpty()) {
                    command.delete()
                }
                else {
                    document.replaceString(param.textRange, replacement)
                }
                documentManager.doPostponedOperationsAndUnblockDocument(document)
                documentManager.commitDocument(document)
            }
        }

        /**
         * Check whether this list item matches [bibName], ignoring comments and whitespace
         */
        private fun String.matchesBibName() = this.trim() != bibName
    }
}
