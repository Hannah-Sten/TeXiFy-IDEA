package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.psi.*

class LatexPsiHelper(private val project: Project) {

    private fun createEnvironmentContent(): LatexEnvironmentContent {
        val environment = createFromText("\\begin{figure}\n" +
                "        Placehodler\n" +
                "    \\end{figure}").firstChildOfType(LatexEnvironment::class)!!
        environment.environmentContent!!.firstChild.delete()
        return environment.environmentContent!!
    }

    private fun createLatexOptionalParam(): LatexParameter {
        return createFromText("\\usepackage[]{package}")
                .findFirstChild { c -> c is LatexParameter && c.optionalParam != null }!!
    }

    fun createLabelCommand(labelName: String): PsiElement {
        val labelText = "\\label{$labelName}"
        val fileFromText = createFromText(labelText)
        return fileFromText.firstChild
    }

    private fun createOptionalParameterContent(parameter: String): List<LatexContent> {
        val commandText = "\\begin{lstlisting}[$parameter]"
        val environment = createFromText(commandText).firstChildOfType(LatexEnvironment::class)!!
        val optionalParam = environment.beginCommand.firstChildOfType(LatexOptionalParam::class)!!
        return optionalParam.openGroup.contentList
    }

    private fun createFromText(text: String): PsiElement =
            PsiFileFactory.getInstance(project).createFileFromText("DUMMY.tex", LatexLanguage.INSTANCE, text, false, true)


    fun addToContent(environment: LatexEnvironment, element: PsiElement, after: PsiElement? = null) {
        if (environment.environmentContent == null) {
            environment.addAfter(createEnvironmentContent(), environment.beginCommand)
        }
        val environmentContent = environment.environmentContent!!

        if (after != null) {
            environmentContent.addAfter(element, after)
        }
        else {
            environmentContent.add(element)
        }
    }

    fun addOptionalParameter(command: LatexBeginCommand, name: String, value: String?) {
        val existingParameters = command.optionalParameters
        if (existingParameters.isEmpty()) {
            command.addAfter(createLatexOptionalParam(), command.parameterList[0])
        }

        val optionalParam = command.parameterList
                .first { p -> p.optionalParam != null }.optionalParam!!

        var parameterText = if (value != null) {
            "$name={$value}"
        }
        else {
            name
        }

        if (existingParameters.isNotEmpty()) {
            parameterText = ",$parameterText";
        }
        val contents = createOptionalParameterContent(parameterText)
        contents.forEach { optionalParam.openGroup.addBefore(it, optionalParam.openGroup.lastChild) }
    }
}