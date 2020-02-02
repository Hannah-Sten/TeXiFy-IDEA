package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexEnvironmentContent

class LatexPsiFactory(private val project: Project) {

    fun createEnvironmentContent(): LatexEnvironmentContent {
        val environment = createFromText("\\begin{figure}\n" +
                "        Placehodler\n" +
                "    \\end{figure}").firstChildOfType(LatexEnvironment::class)!!
        environment.environmentContent!!.firstChild.delete()
        return environment.environmentContent!!
    }

    private fun createFromText(text: String): PsiElement =
            PsiFileFactory.getInstance(project).createFileFromText("DUMMY.tex", LatexLanguage.INSTANCE, text, false, true)

    companion object Labels {
        fun createUniqueLabelCommandFor(latexPsiFactory: LatexPsiFactory, command: LatexCommands): PsiElement? {
            val required = command.requiredParameters
            if (required.isEmpty()) {
                return null
            }

            // Determine label name.
            val prefix = Magic.Command.labeled[command.name]
            val labelName = required[0].formatAsLabel()
            val labelBase = "$prefix:$labelName"

            return createUniqueLabelCommand(latexPsiFactory, labelBase, command.containingFile)
        }

        fun createUniqueLabelCommandFor(latexPsiFactory: LatexPsiFactory, command: LatexEnvironment): PsiElement? {
            // Determine label name.
            val prefix = Magic.Environment.labeled[command.environmentName]
            val labelName = command.environmentName.formatAsLabel()
            val labelBase = "$prefix:$labelName"

            return createUniqueLabelCommand(latexPsiFactory, labelBase, command.containingFile)
        }

        private fun createUniqueLabelCommand(latexPsiFactory: LatexPsiFactory, labelBase: String, file: PsiFile): PsiElement {
            val allLabels = file.findLabelsInFileSet()
            val createdLabel = appendCounter(labelBase, allLabels)
            return createLabelCommand(latexPsiFactory, createdLabel)
        }

        fun createLabelCommand(latexPsiFactory: LatexPsiFactory, labelName: String): PsiElement {
            val labelText = "\\label{$labelName}"
            val fileFromText = latexPsiFactory.createFromText(labelText)
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
}