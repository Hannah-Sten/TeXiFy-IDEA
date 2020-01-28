package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment

class LatexPsiFactory(private val project: Project) {

    fun createUniqueLabelFor(command: LatexCommands): PsiElement? {
        val required = command.requiredParameters
        if (required.isEmpty()) {
            return null
        }

        // Determine label name.
        val prefix = Magic.Command.labeled[command.name]
        val labelName = required[0].formatAsLabel()
        val labelBase = "$prefix:$labelName"

        return createUniqueLabelCommand(labelBase, command.containingFile)
    }

    fun createUniqueLabelFor(command: LatexEnvironment): PsiElement? {
        val required = command.beginCommand.requiredParameters
        if (required.isEmpty()) {
            return null
        }

        // Determine label name.
        val prefix = Magic.Environment.labeled[command.beginCommand.text]
        val labelName = required[0].formatAsLabel()
        val labelBase = "$prefix:$labelName"

        return createUniqueLabelCommand(labelBase, command.containingFile)
    }

    private fun createUniqueLabelCommand(labelBase: String, file: PsiFile): PsiElement {
        val allLabels = file.findLabelsInFileSet()
        val createdLabel = appendCounter(labelBase, allLabels)
        return createLabelCommand(createdLabel)
    }

    fun createLabelCommand(labelName: String): PsiElement {
        val labelText = "\\label{$labelName}"
        val fileFromText = com.intellij.psi.PsiFileFactory.getInstance(project)
                .createFileFromText("DUMMY.tex", LatexLanguage.INSTANCE, labelText, false, true)
        return fileFromText.firstChild
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