package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSFONTS
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSSYMB
import nl.hannahsten.texifyidea.lang.Package.Companion.DEFAULT
import nl.hannahsten.texifyidea.lang.Package.Companion.MATHTOOLS
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.definitionsAndRedefinitionsInFileSet
import java.util.*

/**
 * Currently works for built-in commands and environments.
 *
 * @author Hannah Schellekens
 */
open class LatexMissingImportInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingImport"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Missing imports"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val includedPackages = PackageUtils.getIncludedPackages(file)
        analyseCommands(file, includedPackages, descriptors, manager, isOntheFly)
        analyseEnvironments(file, includedPackages, descriptors, manager, isOntheFly)

        return descriptors
    }

    private fun analyseEnvironments(file: PsiFile, includedPackages: Collection<String>,
                                descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager,
                                isOntheFly: Boolean) {
        val environments = file.childrenOfType(LatexEnvironment::class)
        val defined = file.definitionsAndRedefinitionsInFileSet().asSequence()
                .filter { it.isEnvironmentDefinition() }
                .mapNotNull { it.requiredParameter(0) }
                .toSet()

        for (env in environments) {
            // Don't consider environments that have been defined.
            if (env.name()?.text in defined) {
                continue
            }

            val name = env.name()?.text ?: continue
            val environment = DefaultEnvironment[name] ?: continue
            val pack = environment.dependency

            if (pack == DEFAULT || includedPackages.contains(pack.name)) {
                continue
            }

            // amsfonts is included in amssymb
            if (pack == AMSFONTS && includedPackages.contains(AMSSYMB.name)) {
                continue
            }

            // amsmath is included in mathtools
            if (pack == AMSMATH && includedPackages.contains(MATHTOOLS.name)) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    env,
                    TextRange(7, 7 + name.length),
                    "Environment requires ${pack.name} package",
                    ProblemHighlightType.ERROR,
                    isOntheFly,
                    ImportEnvironmentFix(pack.name)
            ))
        }
    }

    private fun analyseCommands(file: PsiFile, includedPackages: Collection<String>,
                                descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager,
                                isOntheFly: Boolean) {
        val commands = file.commandsInFile()
        for (command in commands) {
            val name = command.commandToken.text.substring(1)
            val latexCommand = LatexCommand.lookup(name) ?: continue
            val pack = latexCommand.dependency

            if (pack.isDefault) {
                continue
            }

            // amsfonts is included in amssymb
            if (pack == AMSFONTS && includedPackages.contains(AMSSYMB.name)) {
                continue
            }

            // amsmath is included in mathtools
            if (pack == AMSMATH && includedPackages.contains(MATHTOOLS.name)) {
                continue
            }

            if (!includedPackages.contains(pack.name)) {
                descriptors.add(manager.createProblemDescriptor(
                        command,
                        TextRange(0, latexCommand.command.length + 1),
                        "Command requires ${pack.name} package",
                        ProblemHighlightType.ERROR,
                        isOntheFly,
                        ImportCommandFix(pack.name)
                ))
            }
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private class ImportCommandFix(val import: String) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package '$import'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val latexCommand = LatexCommand.lookup(command.commandToken.text) ?: return
            val file = command.containingFile

            PackageUtils.insertUsepackage(file, latexCommand.dependency)
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private class ImportEnvironmentFix(val import: String) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package '$import'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val environment = descriptor.psiElement as? LatexEnvironment ?: return
            val thingy = DefaultEnvironment.fromPsi(environment) ?: return
            val file = environment.containingFile

            PackageUtils.insertUsepackage(file, thingy.dependency)
        }
    }
}