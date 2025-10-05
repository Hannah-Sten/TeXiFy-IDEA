package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.magic.GeneralMagic
import nl.hannahsten.texifyidea.util.parser.traverseTyped
import java.util.*

class LatexPackageCouldNotBeFound : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String =
        "PackageCouldNotBeFound"

    override fun getDisplayName(): String {
        return "Package could not be found locally or on CTAN"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val ctanPackages = PackageUtils.CTAN_PACKAGE_NAMES.map { it.lowercase(Locale.getDefault()) }
        val customPackages = NewCommandsIndex.getByName(LatexGenericRegularCommand.PROVIDESPACKAGE.commandWithSlash, file.project)
            .map { it.requiredParameterText(0) }
            .map { it?.lowercase(Locale.getDefault()) }
        val packages = ctanPackages + customPackages

        val commands = file.traverseTyped<LatexCommands>()
            .filter { it.name == LatexGenericRegularCommand.USEPACKAGE.commandWithSlash || it.name == LatexGenericRegularCommand.REQUIREPACKAGE.commandWithSlash }

        for (command in commands) {
            @Suppress("ktlint:standard:property-naming")
            val `package` = command.requiredParameterText(0)?.lowercase(Locale.getDefault())
            if (!packages.contains(`package`)) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        command,
                        displayName,
                        GeneralMagic.noQuickFix,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }
        }

        return descriptors
    }
}