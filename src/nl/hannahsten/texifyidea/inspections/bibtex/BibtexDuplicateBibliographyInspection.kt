package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.includedPackagesInFileset

/**
 * @author Sten Wessel
 */
open class BibtexDuplicateBibliographyInspection : TexifyInspectionBase() {

    // Manual override to match short name in plugin.xml
    override fun getShortName() = InsightGroup.BIBTEX.prefix + inspectionId

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "DuplicateBibliography"

    override fun getDisplayName() = "Same bibliography is included multiple times"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        // Chapterbib allows multiple bibliographies
        if (file.includedPackagesInFileset().any { it == LatexPackage.CHAPTERBIB }) {
            return emptyList()
        }

        val descriptors = descriptorList()

        // Map each bibliography file to all the commands which include it
        val groupedIncludes = mutableMapOf<Pair<String, String>, MutableList<LatexCommands>>()

        val commands = NewCommandsIndex.getByNameInFileSet("\\bibliography", file).asSequence() +
            NewCommandsIndex.getByNameInFileSet("\\addbibresource", file)
        commands.forEach { command ->
            for ((filePath, fileName) in InputFileReference.getIncludedFiles(command, false).map { it.virtualFile.path to it.name }) {
                groupedIncludes.getOrPut(filePath to fileName) { mutableListOf() }.add(command)
            }
        }

        groupedIncludes.asSequence()
            .filter { (_, commands) -> commands.size > 1 }
            .forEach { (fileKey, commands) ->
                for (command in commands.distinct()) {
                    if (command.containingFile != file) continue
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            "Bibliography file '${fileKey.second}' is included multiple times",
                            RemoveOtherCommandsFix(fileKey.second, commands.map { it.createSmartPointer() }),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                        )
                    )
                }
            }
        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    class RemoveOtherCommandsFix(private val bibName: String, private val commandsToFix: List<SmartPsiElementPointer<LatexCommands>>) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Remove other includes of $bibName"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val currentCommand = descriptor.psiElement as LatexCommands
            commandsToFix.map { it.element }.filterNot { it == currentCommand || it == null }.forEach {
                it?.parent?.node?.removeChild(it.node)
            }
        }

        override fun generatePreview(project: Project, descriptor: ProblemDescriptor): IntentionPreviewInfo {
            // Removes elements that are not at the cursor, so showing the diff of removing the element which also happens to be at the cursor can be confusing.
            return IntentionPreviewInfo.EMPTY
        }
    }
}
