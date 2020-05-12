package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.util.findFirstChild
import nl.hannahsten.texifyidea.util.firstChildOfType

class LatexPsiHelper(private val project: Project) {

    private fun createEnvironmentContent(): LatexEnvironmentContent {
        val environment = createFromText("\\begin{figure}\n" +
                "        Placeholder\n" +
                "    \\end{figure}").firstChildOfType(LatexEnvironment::class)!!
        environment.environmentContent!!.firstChild.delete()
        return environment.environmentContent!!
    }

    private fun createLatexOptionalParam(): LatexParameter {
        return createFromText("\\usepackage[]{package}")
                .findFirstChild { c -> c is LatexParameter && c.optionalParam != null }!!
    }

    /**
     * Create a label command \label{labelName}.
     */
    fun createLabelCommand(labelName: String): PsiElement {
        val labelText = "\\label{$labelName}"
        val fileFromText = createFromText(labelText)
        return fileFromText.firstChild
    }

    private fun createOptionalParameterContent(parameter: String): List<LatexParamContent> {
        val commandText = "\\begin{lstlisting}[$parameter]"
        val environment = createFromText(commandText).firstChildOfType(LatexEnvironment::class)!!
        val optionalParam = environment.beginCommand.firstChildOfType(LatexOptionalParam::class)!!
        return optionalParam.paramContentList
    }

    fun createFromText(text: String): PsiElement =
            PsiFileFactory.getInstance(project).createFileFromText("DUMMY.tex", LatexLanguage.INSTANCE, text, false, true)

    /**
     * Adds the supplied element to the content of the environment.
     * @param environment The environment whose content should be manipulated
     * @param element The element to be inserted
     * @param after If specified, the new element will be inserted after this element
     * @return The new element in the PSI tree. Note that this element is *not* necessarily equal
     * to the supplied element. For example, the new element might have an updated endOffset
     */
    fun addToContent(environment: LatexEnvironment, element: PsiElement, after: PsiElement? = null): PsiElement {
        if (environment.environmentContent == null) {
            environment.addAfter(createEnvironmentContent(), environment.beginCommand)
        }
        val environmentContent = environment.environmentContent!!

        return if (after != null) {
            environmentContent.addAfter(element, after)
        }
        else {
            environmentContent.add(element)
        }
    }

    fun createRequiredParameter(content: String): LatexRequiredParam {
        val commandText = "\\label{$content}"
        return createFromText(commandText).firstChildOfType(LatexRequiredParam::class)!!
    }

    /**
     * Replaces the optional parameter with the supplied name. If a value is supplied, the new parameter will
     * have a value appended with an equal sign (e.g., param={value}). The new parameter has the same position
     * as the old one. If no parameter with the supplied name is found, no action will be performed.
     */
    fun replaceOptionalParameter(parameters: List<LatexParameter>, name: String, newValue: String?) {
        val optionalParam = parameters.first { p -> p.optionalParam != null }.optionalParam!!

        val parameterText = if (newValue != null) {
            "$name={$newValue}"
        }
        else {
            name
        }

        val labelRegex = "label\\s*=\\s*[^,]*".toRegex()
        val elementsToReplace = mutableListOf<LatexParamContent>()
        val elementIterator = optionalParam.paramContentList.iterator()
        while (elementIterator.hasNext()) {
            val latexContent = elementIterator.next()
            val elementIsLabel = latexContent.parameterText?.text?.contains(labelRegex) ?: false
            if (elementIsLabel) {
                elementsToReplace.add(latexContent)

                // check if the label name is part of the text or in a separate group
                if (latexContent.parameterText!!.text.split("=")[1].trim().isEmpty()) {
                    val group = elementIterator.next()
                    elementsToReplace.add(group)
                }
            }
        }

        val newContents = createOptionalParameterContent(parameterText)
        newContents.forEach { optionalParam.addBefore(it, elementsToReplace.first()) }
        elementsToReplace.forEach { it.delete() }
    }

    /**
     * Add an optional parameter of the form "param" or "param={value}" to the list of optional parameters.
     * If there already are optional parameters, the new parameter will be appended with a "," as the separator.
     *
     * @return A list containing the newly inserted elements from left to right
     */
    fun addOptionalParameter(command: LatexBeginCommand, name: String, value: String?): List<PsiElement> {
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
            parameterText = ",$parameterText"
        }
        val newElements = mutableListOf<PsiElement>()
        val contents = createOptionalParameterContent(parameterText)
        contents.forEach {
            val inserted = optionalParam.addBefore(it, optionalParam.lastChild)
            newElements.add(inserted)
        }
        return newElements
    }
}