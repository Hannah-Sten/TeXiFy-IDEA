package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.*
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
                "\\subsection", "subsec",
                "\\item", "itm"
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
            return sibling.firstChildOfType(LatexCommands::class)
        }
    }

    override fun getDisplayName(): String {
        return "Label conventions"
    }

    override fun getInspectionId(): String {
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

    private fun checkEnvironments(file: PsiFile) {
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
            val required = command.firstChildOfType(LatexRequiredParam::class) ?: return
            val oldLabel = required.firstChildOfType(LatexNormalText::class)?.text ?: return

            // Determine label name.
            val prefix: String = LABELED_COMMANDS[context.name] ?: return
            val labelName = oldLabel.camelCase()
            val createdLabelBase = if (labelName.contains(":")) {
                LABEL_PREFIX.matcher(labelName).replaceAll("$prefix:")
            }
            else {
                "$prefix:$labelName"
            }

            val createdLabel = appendCounter(createdLabelBase, TexifyUtil.findLabelsInFileSet(file))

            // Replace in document.
            val references = findReferences(file, oldLabel)
            references.add(required)
            references.sortWith(Comparator { obj, anotherInteger -> anotherInteger.endOffset().compareTo(obj.endOffset()) })

            for (reference in references) {
                document.replaceString(reference.textRange, "{$createdLabel}")
            }
        }

        /**
         * Find all references to label `labelName`.
         */
        private fun findReferences(file: PsiFile, labelName: String): MutableList<LatexRequiredParam> {
            val resultList = ArrayList<LatexRequiredParam>()

            val commands = file.commandsInFileSet().filter { it.name == "\\ref" || it.name == "\\cite" }.reversed()
            for (ref in commands) {
                val parameter = ref.firstChildOfType(LatexRequiredParam::class) ?: continue
                val name = parameter.firstChildOfType(LatexNormalText::class)?.text ?: continue

                if (name == labelName) {
                    resultList.add(parameter)
                }
            }

            return resultList
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