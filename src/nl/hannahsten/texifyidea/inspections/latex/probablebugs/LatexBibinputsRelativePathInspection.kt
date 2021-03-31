package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getBibtexRunConfigurations
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.requiredParameter
import nl.hannahsten.texifyidea.util.requiredParameters

/**
 * BIBINPUTS cannot handle paths which start with ../  in the \bibliography command, e.g. \bibliography{../mybib}.
 * Solution: set the BIBINPUTS path to the parent and use \bibliography{mybib} instead (or use a fake subfolder and do \bibliography{fake/../../mybib}
 * See https://tex.stackexchange.com/questions/406024/relative-paths-with-bibinputs
 */
class LatexBibinputsRelativePathInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "BibinputsRelativePath"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val commandsWithRelativePath = file.commandsInFile(LatexGenericRegularCommand.BIBLIOGRAPHY.cmd).filter { it.requiredParameter(0)?.startsWith("../") == true }
        if (commandsWithRelativePath.isEmpty()) return emptyList()

        // If not using BIBINPUTS, all is fine and it will work.
        val usesBibinputs = file.getBibtexRunConfigurations()
            .any { config -> config.environmentVariables.envs.keys.any { it == "BIBINPUTS" } }

        if (!usesBibinputs) return emptyList()

        val descriptors = descriptorList()
        commandsWithRelativePath.forEach { command ->
            descriptors.add(
                manager.createProblemDescriptor(
                    command,
                    TextRange(0, command.textLength - 1),
                    "You cannot use both BIBINPUTS and a path that starts with ../ in \\bibliography",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    RelativePathFix
                )
            )
        }
        return descriptors
    }

    object RelativePathFix : LocalQuickFix {
        override fun getFamilyName() = "Remove relative part from argument and from BIBINPUTS"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val oldNode = descriptor.psiElement.node
            val newText = oldNode.text.replaceFirst("../", "")
            val newNode = LatexPsiHelper(project).createFromText(newText).firstChild.node ?: return
            descriptor.psiElement.parent.node.replaceChild(oldNode, newNode)

            // Fix BIBINPUTS
            project
                .getLatexRunConfigurations()
                .filter { it.mainFile == descriptor.psiElement.containingFile.findRootFile().virtualFile }
                .flatMap { it.bibRunConfigs }
                .map { it.configuration }
                .filterIsInstance<BibtexRunConfiguration>()
                .forEach { config ->
                    val envs = config.environmentVariables.envs.toMutableMap()
                    val oldPath = envs["BIBINPUTS"] ?: return@forEach
                    val newPath = oldPath.substring(0, oldPath.lastIndexOf('/'))
                    envs["BIBINPUTS"] = newPath
                    config.environmentVariables = EnvironmentVariablesData.create(envs, config.environmentVariables.isPassParentEnvs)
                }
        }
    }
}