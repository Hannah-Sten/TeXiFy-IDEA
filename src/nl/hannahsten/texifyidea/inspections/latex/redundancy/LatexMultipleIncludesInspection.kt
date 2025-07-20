package nl.hannahsten.texifyidea.inspections.latex.redundancy

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.getParameterTexts
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexMultipleIncludesInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MultipleIncludes"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.ENVIRONMENT, MagicCommentScope.MATH_ENVIRONMENT)!!

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Package has been imported multiple times"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        // Find all explicit imported packages in the fileset
        val packagesWithDuplicate = PackageUtils.getExplicitlyIncludedPackagesInFileset(file)
        // When using the subfiles package, there will be multiple \documentclass{subfiles} commands
        val ignoredPackages = setOf(LatexPackage.SUBFILES.name)
        val packages = mutableSetOf<String>()
        val duplicates = mutableSetOf<String>()
        packagesWithDuplicate.filterNotTo(duplicates) {
            packages.add(it) || it in ignoredPackages
        }

        // Duplicates!
        val descriptors = file.traverseCommands()
            .filter { it.name == LatexGenericRegularCommand.USEPACKAGE.cmd }
            .filterNot { it.parent?.firstParentOfType<LatexCommands>(7)?.name == LatexGenericRegularCommand.ONLYIFSTANDALONE.commandWithSlash }
            .flatMap { it.getParameterTexts() }
            .filter { it.text in duplicates }
            .map {
                manager.createProblemDescriptor(
                    it,
                    null,
                    "Package has already been included",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            }
            .toList()

        return descriptors
    }
}