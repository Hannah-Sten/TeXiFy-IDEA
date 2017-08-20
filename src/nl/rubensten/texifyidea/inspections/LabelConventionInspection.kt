package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.psi.LatexPsiUtil
import nl.rubensten.texifyidea.util.*
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * Currently only works for Chapters, Sections and Subsections.
 *
 * Planned is to also implement this for other environments.
 *
 * @author Ruben Schellekens
 */
open class LabelConventionInspection : TexifyInspectionBase() {

    companion object {

        /**
         * Map that maps all commands that are expected to have a label to the label prefix they have by convention.
         *
         * command name `=>` label prefix without colon
         */
        val LABELED_COMMANDS = mapOfVarargs(
                "\\chapter", "ch",
                "\\section", "sec",
                "\\subsection", "subsec"
        )

        /**
         * Map that maps all environments that are expected to have a label to the label prefix they have by convention.
         *
         * environment name `=>` label prefix without colon
         */
        val LABELED_ENVIRONMENTS = mapOfVarargs(
                "figure", "fig",
                "table", "tab",
                "tabular", "tab",
                "equation", "eq",
                "algorithm", "alg"
        )

        @Language("RegExp")
        private val LABEL_PREFIX = Pattern.compile(".*:")

        /**
         * Looks for the command that the label is a definition for.
         */
        private fun findContextCommand(label: LatexCommands): LatexCommands? {
            val grandparent = label.parent.parent
            val sibling = LatexPsiUtil.getPreviousSiblingIgnoreWhitespace(grandparent) ?: return null

            val commands = sibling.childrenOfType(LatexCommands::class)
            return if (commands.isEmpty()) null else commands.first()
        }
    }

    override fun getDisplayName(): String {
        return "Label conventions"
    }

    override fun getShortName(): String {
        return "LabelConvention"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        checkCommands(file, manager, isOntheFly, descriptors)
        // checkEnvironments(file, manager, isOntheFly, descriptors)

        return descriptors
    }

    private fun checkCommands(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean,
                              descriptors: MutableList<ProblemDescriptor>) {
        val commands = file.commandsInFile()
        for (cmd in commands) {
            if (cmd.name != "\\label") {
                continue
            }

            val required = cmd.requiredParameters
            if (required.isEmpty()) {
                continue
            }

            val context = findContextCommand(cmd) ?: continue
            if (!LABELED_COMMANDS.containsKey(context.name)) {
                continue
            }

            val prefix = LABELED_COMMANDS[context.name]!!
            if (!required[0].startsWith("$prefix:")) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        "Unconventional label prefix",
                        LabelPreFix(),
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly
                ))
            }
        }
    }

    private fun checkEnvironments(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean,
                                  descriptors: MutableList<ProblemDescriptor>) {
        val environments = file.childrenOfType(LatexEnvironment::class)
        for (env in environments) {
            val parameters = env.beginCommand.parameterList
            if (parameters.isEmpty()) {
                continue
            }

            val environmentName = parameters[0].requiredParam?.group?.contentList!![0].text ?: continue
            if (!LABELED_ENVIRONMENTS.containsKey(environmentName)) {
                continue
            }

            val labelMaybe = env.childrenOfType(LatexContent::class).stream()
                    .filter { it.childrenOfType(LatexEnvironment::class).isEmpty() }
                    .flatMap { it.childrenOfType(LatexCommands::class).stream() }
                    .filter { it.name == "\\label" }
        }

        // TODO: make this work. but it's a bit dodgy..
    }

    /**
     * @author Ruben Schellekens
     */
    private class LabelPreFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Fix label name"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val context = findContextCommand(command) ?: return
            val file = command.containingFile
            val document = file.document() ?: return
            val required = command.requiredParameters
            if (required.isEmpty()) {
                return
            }

            // Determine label name.
            val prefix: String = LABELED_COMMANDS[context.name] ?: return
            val labelName = required[0].camelCase()
            val createdLabelBase = if (labelName.contains(":")) {
                LABEL_PREFIX.matcher(labelName).replaceAll("$prefix:")
            }
            else {
                "$prefix:$labelName"
            }

            val createdLabel = appendCounter(createdLabelBase, TexifyUtil.findLabelsInFileSet(file))

            // Replace in document.
            val offset = command.textOffset + 7
            val length = required[0].length
            document.replaceString(offset, offset + length, createdLabel)
        }

        /**
         * Keeps adding a counter behind the label until there is no other label with that name.
         */
        private fun appendCounter(label: String, allLabels: Set<String>): String {
            var counter = 2
            var candidate = label

            while (allLabels.contains(candidate)) {
                candidate = label + (counter++)
            }

            return candidate
        }
    }
}