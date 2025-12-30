package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.jetbrains.rd.util.reflection.threadLocal
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyContextAwareInspection
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.intentions.LatexAddLabelToCommandIntention
import nl.hannahsten.texifyidea.intentions.LatexAddLabelToEnvironmentIntention
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsConfigurable
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.util.files.documentClass
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.openedTextEditor
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.getOptionalParameterMapFromParameters
import nl.hannahsten.texifyidea.util.parser.lookupCommandPsi
import nl.hannahsten.texifyidea.util.parser.toStringMap
import org.jetbrains.annotations.Nls

/**
 * Check for commands which should have a label but don't.
 *
 * @author Hannah Schellekens
 */
class LatexMissingLabelInspection : AbstractTexifyContextAwareInspection(
    inspectionId = "MissingLabel",
    inspectionGroup = InsightGroup.LATEX,
    applicableContexts = null,
    excludedContexts = setOf(),
    skipChildrenInContext = setOf(LatexContexts.Comment, LatexContexts.InsideDefinition)
) {

    private var labeledCommandsLocal: Set<String>? by threadLocal { null }

    private var labeledEnvironmentsLocal: Set<String>? by threadLocal { null }

    override fun prepareInspectionForFile(file: PsiFile, bundle: DefinitionBundle): Boolean {
        val settings = TexifyConventionsSettingsManager.getInstance(file.project).getSettings()

        val requireLabel = settings.currentScheme.labelConventions.filter { it.enabled }
        val labeledCommands =
            requireLabel.filter { c -> c.type == LabelConventionType.COMMAND }.map { "\\" + it.name }.toMutableSet()

        // Document classes like book and report provide \part as sectioning, but with exam class it's a part in a question
        if (file.findRootFile().documentClass() == LatexLib.EXAM.name) {
            labeledCommands.remove("\\part")
        }
        labeledCommandsLocal = labeledCommands

        labeledEnvironmentsLocal =
            requireLabel.filter { c -> c.type == LabelConventionType.ENVIRONMENT }.map { it.name }.toSet()

        return true
    }

    override fun inspectElement(element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        when(element) {
            is LatexCommands -> inspectCommand(element, contexts, bundle, file, manager, isOnTheFly, descriptors)
            is LatexEnvironment -> inspectEnvironment(element, contexts, bundle, file, manager, isOnTheFly, descriptors)
        }
    }

    private fun LatexCommands.hasLabel(defBundle: DefinitionBundle): Boolean {
        if (CommandMagic.labelAsParameter.contains(this.name)) {
            return getOptionalParameterMapFromParameters(this.parameterList).toStringMap().containsKey("label")
        }

        // Next leaf is a command token, parent is LatexCommands
        val nextCommand = this.nextContextualSiblingIgnoreWhitespace() as? LatexCommands ?: return false

        val semantics = defBundle.lookupCommandPsi(nextCommand) ?: return false
        return semantics.arguments.any { it.contextSignature.introduces(LatexContexts.LabelDefinition) }
    }

    /**
     * Adds a command descriptor to the given command if there is a label missing.
     */
    private fun inspectCommand(
        command: LatexCommands, contexts: LContextSet,
        defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        val nameWithSlash = command.nameWithSlash
        if (labeledCommandsLocal?.contains(nameWithSlash) != true) return
        if (command.hasStar()) return
        if (command.hasLabel(defBundle)) return

        val fixes = mutableListOf<LocalQuickFix>()
        fixes.add(InsertLabelForCommandFix())
        if (!CommandMagic.labelAsParameter.contains(command.name)) {
            fixes.add(ChangeMinimumLabelLevelFix())
        }

        // For adding the label, see LatexAddLabelIntention
        descriptors.add(
            manager.createProblemDescriptor(
                command,
                "Missing label",
                fixes.toTypedArray(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
                false
            )
        )
    }

    private fun inspectEnvironment(
        environment: LatexEnvironment, contexts: LContextSet,
        defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        val name = environment.getEnvironmentName()
        if (labeledEnvironmentsLocal?.contains(name) != true) return
        if (environment.getLabel() != null) return

        descriptors.add(
            manager.createProblemDescriptor(
                environment,
                "Missing label",
                arrayOf(InsertLabelInEnvironmentFix()),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
                false
            )
        )
    }

    /**
     * Open the settings page so the user can change the minimum labeled level.
     */
    private class ChangeMinimumLabelLevelFix : LocalQuickFix {

        @Nls
        override fun getFamilyName(): String = "Change label conventions"

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, TexifyConventionsConfigurable::class.java)
        }
    }

    /**
     * This is also an intention, but in order to keep the same alt+enter+enter functionality (because we have an other
     * quickfix as well) we keep it as a quickfix also.
     */
    private class InsertLabelForCommandFix : LocalQuickFix {

        // It has to appear in alphabetical order before the other quickfix
        override fun getFamilyName() = "Add label for this command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            LatexAddLabelToCommandIntention(command.createSmartPointer()).invoke(
                project,
                command.containingFile.openedTextEditor(),
                command.containingFile
            )
        }
    }

    private class InsertLabelInEnvironmentFix : LocalQuickFix {

        override fun getFamilyName() = "Add label for this environment"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexEnvironment
            LatexAddLabelToEnvironmentIntention(command.createSmartPointer()).invoke(
                project,
                command.containingFile.openedTextEditor(),
                command.containingFile
            )
        }
    }
}