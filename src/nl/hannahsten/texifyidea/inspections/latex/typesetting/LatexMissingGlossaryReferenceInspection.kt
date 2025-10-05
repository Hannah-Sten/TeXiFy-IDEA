package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.parser.traverseTyped
import nl.hannahsten.texifyidea.util.toTextRange

/**
 * Glossary entries should be referenced for all occurrences.
 */
class LatexMissingGlossaryReferenceInspection : TexifyInspectionBase() {
    override val inspectionGroup = InsightGroup.LATEX
    override val inspectionId = "MissingGlossaryReference"
    override fun getDisplayName() = "Missing glossary or acronym reference"

    private val nameLetterRegex = "[^a-zA-Z]+".toRegex()

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val entries = NewSpecialCommandsIndex.getAllGlossaryEntries(file)
        val extractedNames = entries.asSequence().mapNotNull { LatexGlossariesCommand.extractGlossaryName(it) }
            .map {
                // Ensure the regex is valid, assuming that regular words don't contain e.g. braces
                it.replace(nameLetterRegex, "")
            }.filter {
                it.isNotBlank()
            }.toList()
        if (extractedNames.isEmpty()) {
            // No valid glossary names, so no need to check for missing references
            return emptyList()
        }
        val descriptors = descriptorList()
        val regexes = extractedNames.map { nameLetters ->
            val nameLetterRegex = nameLetters.toRegex()
            // Both glossaries and acronym packages provide acronymsh
            val glsRegex = "\\\\(?:gls|ac)[^{]+\\{($nameLetters)}".toRegex()
            nameLetterRegex to glsRegex
        }
        val libraries = LatexProjectStructure.getFilesetDataFor(file)?.libraries ?: emptySet()

        // Unfortunately the lowest level we have is a block of text, so we have to do a text-based search
        file.traverseTyped<LatexNormalText>().forEach { textElement ->
            val text = textElement.text
            regexes.forEach { (nameLettersRegex, glsRegex) ->
                val correctOccurrences = glsRegex.findAll(text).mapNotNull { it.groups.firstOrNull()?.range }
                val allOccurrences = nameLettersRegex.findAll(text).map { it.range }
                allOccurrences.filter { !correctOccurrences.contains(it) }.forEach { range ->
                    // The command is different for each package, but the idea is the same
                    val fixes = listOf(Pair("glossaries.sty", "\\gls"), Pair("acronym.sty", "\\ac")).filter { it.first in libraries }
                        .map { AddGlsFix(it.second) }
                        .toTypedArray()
                        .ifEmpty { arrayOf(AddGlsFix("\\gls")) }

                    descriptors.add(
                        manager.createProblemDescriptor(
                            textElement,
                            range.toTextRange(),
                            "Missing glossary or acronym reference",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly,
                            *fixes,
                        )
                    )
                }
            }
        }
        return descriptors
    }

    private class AddGlsFix(private val command: String) : LocalQuickFix {
        override fun getFamilyName() = "Add $command command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val range = descriptor.textRangeInElement
            val newText = descriptor.psiElement.text.replaceRange(range.endOffset, range.endOffset, "}")
                .replaceRange(range.startOffset, range.startOffset, "$command{")

            val newElement = LatexPsiHelper(project).createFromText(newText).firstChild
            descriptor.psiElement.replace(newElement)
        }
    }
}