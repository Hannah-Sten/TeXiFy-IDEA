package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.lang.LatexCommand
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.PackageUtils
import nl.rubensten.texifyidea.util.commandsInFileSet
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * Currently only works for built-in commands.
 *
 * @author Ruben Schellekens
 */
open class MissingImportInspection : TexifyInspectionBase() {


    override fun getDisplayName(): String {
        return "Missing imports"
    }

    override fun getShortName(): String {
        return "MissingImport"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val includedPackages = PackageUtils.getIncludedPackages(file)
        val commands = file.commandsInFileSet()
        for (cmd in commands) {
            val latexCommand = LatexCommand.lookup(cmd.name) ?: continue
            val pack = latexCommand.`package`

            if (pack.isDefault) {
                continue
            }

            if (!includedPackages.contains(pack.name)) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        "Command requires ${pack.name} package",
                        ImportFix(),
                        ProblemHighlightType.ERROR,
                        isOntheFly
                ))
            }
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private class ImportFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Add import"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val latexCommand = LatexCommand.lookup(command.name) ?: return
            val file = command.containingFile

            PackageUtils.insertUsepackage(file, latexCommand.`package`)
        }
    }
}