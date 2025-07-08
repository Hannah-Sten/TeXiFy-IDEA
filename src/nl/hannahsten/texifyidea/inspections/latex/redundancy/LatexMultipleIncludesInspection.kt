package nl.hannahsten.texifyidea.inspections.latex.redundancy

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import java.util.*
import kotlin.collections.HashSet

/**
 * @author Hannah Schellekens
 */
open class LatexMultipleIncludesInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MultipleIncludes"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.ENVIRONMENT, MagicCommentScope.MATH_ENVIRONMENT)!!

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Package has been imported multiple times"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Find all duplicates.
        val scope = LatexProjectStructure.getFilesetScopeFor(file)
        val inclusionCommands = NewSpecialCommandsIndex.getAllPackageIncludes(file.project, scope)
        val packages = PackageUtils.getPackagesFromCommands(inclusionCommands, CommandMagic.packageInclusionCommands, mutableListOf())
        // When using the subfiles package, there will be multiple \documentclass{subfiles} commands
        val ignoredPackages = setOf(LatexPackage.SUBFILES.name)
        val covered = HashSet<String>()
        val duplicates = HashSet<String>()
        packages.filterNotTo(duplicates) {
            covered.add(it) || it in ignoredPackages
        }

        // Duplicates!
        file.traverseCommands()
            .filter { it.name == LatexGenericRegularCommand.USEPACKAGE.cmd && it.requiredParameterText(0) in duplicates }
            .forEach {
                val parameter = it.findFirstChildOfType(LatexRequiredParam::class) ?: error("There must be a required parameter.")
                descriptors.add(
                    manager.createProblemDescriptor(
                        it,
                        TextRange.from(parameter.textOffset + 1 - it.textOffset, parameter.textLength - 2),
                        "Package has already been included",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }

        return descriptors
    }
}