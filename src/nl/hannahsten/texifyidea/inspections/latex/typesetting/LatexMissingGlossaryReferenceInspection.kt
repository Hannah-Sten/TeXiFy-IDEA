package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import nl.hannahsten.texifyidea.util.toTextRange

/**
 * Glossary entries should be referenced for all occurrences.
 */
class LatexMissingGlossaryReferenceInspection : TexifyInspectionBase() {
    override val inspectionGroup = InsightGroup.LATEX
    override val inspectionId = "MissingGlossaryReference"
    override fun getDisplayName() = "Missing glossary reference"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = mutableListOf<ProblemDescriptor>()

        val names = NewSpecialCommandsIndex.getAllGlossaryEntries(file).mapNotNull { LatexGlossariesCommand.extractGlossaryName(it) }
        // Unfortunately the lowest level we have is a block of text, so we have to do a text-based search
        file.collectSubtreeTyped<LatexNormalText>().forEach { textElement ->
            val text = textElement.text
            names.forEach { name ->
                // Ensure the regex is valid, assuming that regular words don't contain e.g. braces
                val nameLetters = name.replace("[^a-zA-Z]+".toRegex(), "")
                val correctOccurrences = "\\\\gls[^{]+\\{($nameLetters)}".toRegex().findAll(text).mapNotNull { it.groups.firstOrNull()?.range }
                val allOccurrences = nameLetters.toRegex().findAll(text).map { it.range }
                allOccurrences.filter { !correctOccurrences.contains(it) }.forEach { range ->
                    descriptors.add(
                        manager.createProblemDescriptor(
                            textElement,
                            range.toTextRange(),
                            "Missing glossary reference",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly,
                            AddGlsFix(),
                        )
                    )
                }
            }
        }
        return descriptors
    }

    private class AddGlsFix : LocalQuickFix {
        override fun getFamilyName() = "Add \\gls command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val range = descriptor.textRangeInElement
            val newText = descriptor.psiElement.text.replaceRange(range.endOffset, range.endOffset, "}")
                .replaceRange(range.startOffset, range.startOffset, "\\gls{")

            val newElement = LatexPsiHelper(project).createFromText(newText).firstChild
            descriptor.psiElement.replace(newElement)
        }
    }
}