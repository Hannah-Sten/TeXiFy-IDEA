package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexTypes.*
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped

/**
 * As the IntelliJ SDK docs say, to replace or insert text it is easiest to create a dummy file,
 * fill it with the desired text, extract the psi elements and put them in the actual psi tree.
 * Don't use document.insertString because that will lead to various things (like inspections) working with an obsolete psi tree.
 */
class LatexPsiHelper(private val project: Project) {

    private fun createEnvironmentContent(): LatexEnvironmentContent {
        val environment = createFromText(
            "\\begin{figure}\n" +
                "        Placeholder\n" +
                "    \\end{figure}"
        ).findFirstChildOfType(LatexEnvironment::class)!!
        environment.environmentContent!!.firstChild.delete()
        return environment.environmentContent!!
    }

    private fun createLatexOptionalParam(): LatexParameter {
        return createFromText("\\usepackage[]{package}")
            .findFirstChildTyped<LatexParameter> { c -> c.optionalParam != null }!!
    }

    /**
     * Create a label command \label{labelName}.
     */
    fun createLabelCommand(labelName: String): PsiElement {
        val labelText = "\\label{$labelName}"
        val fileFromText = createFromText(labelText)
        return fileFromText.firstChild
    }

    private fun createKeyValuePairs(parameter: String): LatexOptionalKeyValPair {
        val commandText = "\\begin{lstlisting}[$parameter]"
        val environment = createFromText(commandText).findFirstChildOfType(LatexEnvironment::class)!!
        val optionalParam = environment.beginCommand.findFirstChildOfType(LatexOptionalParam::class)!!
        return optionalParam.optionalKeyValPairList[0]
    }

    /**
     * Create a PsiFile containing the given text.
     */
    fun createFromText(text: String): PsiFile = createFromText(text,project)

    fun createBibtexFromText(text: String): PsiFile =
        PsiFileFactory.getInstance(project).createFileFromText("DUMMY.bib", BibtexLanguage, text, false, true)

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

    fun createRequiredParameter(content: String, hasBraces: Boolean = false): LatexRequiredParam {
        // The command does not matter, we just need one to get it parsed as an argument
        val commandText = if (hasBraces) "\\label$content" else "\\label{$content}"
        return createFromText(commandText).findFirstChildOfType(LatexRequiredParam::class)!!
    }

    fun createOptionalParameter(content: String): LatexOptionalParam? {
        val commandText = "\\section[$content]{$content}"
        return createFromText(commandText).findFirstChildOfType(LatexOptionalParam::class)
    }

    /**
     * Returns the LatexOptionalParam node that is supposed to contain the label key for the command.
     * If no such node exists yet, a new one is created at the correct position.
     */
    private fun getOrCreateLabelOptionalParameters(command: LatexCommandWithParams): LatexOptionalParam {
        // This is only a heuristic. We would actually need detailed information on which optional parameter is
        // supposed to hold the label key.
        val existingParameters = command.getOptionalParameterMap()
        if (existingParameters.isEmpty()) {
            if (command is LatexCommands) {
                // For commands insert an optional parameter right after the command name (in case the command has a
                // star, insert the parameter after the start)
                command.addAfter(
                    createLatexOptionalParam(),
                    command.findFirstChildTyped<LeafPsiElement> { it.elementType == STAR } ?: command.commandToken
                )
            } else if(command is LatexBeginCommand) {
                // Otherwise assume that the command belongs to an environment and insert the optional parameter after
                // the first parameter (which is the environment name)
                command.addAfter(createLatexOptionalParam(), command.envIdentifier!!.nextSibling)
            }
        }

        return command.parameterList
            .first { p -> p.optionalParam != null }.optionalParam!!
    }

    /**
     * Set the value of the optional parameter with the given key name. If the parameter already exists,
     * its value is changed. If no key with the given name exists yet, a new one is created with the given value.
     *
     * @param name The name of the parameter to change
     * @param value The new parameter value. If the value is null, the parameter will have a key only.
     */
    fun setOptionalParameter(command: LatexCommandWithParams, name: String, value: String?): LatexOptionalKeyValPair? {
        val optionalParam = getOrCreateLabelOptionalParameters(command)

        val parameterText = if (value != null) {
            "$name=$value"
        }
        else {
            name
        }

        val pair = createKeyValuePairs(parameterText)
        val closeBracket = optionalParam.findFirstChildTyped<LeafPsiElement> { it.elementType == CLOSE_BRACKET }
        return if (optionalParam.optionalKeyValPairList.isNotEmpty()) {
            val existing = optionalParam.optionalKeyValPairList.find { kv -> kv.optionalKeyValKey.text == name }
            if (existing != null && pair.keyValValue != null) {
                existing.keyValValue?.delete()
                existing.addAfter(
                    pair.keyValValue!!,
                    existing.findFirstChildTyped<LeafPsiElement> { it.elementType == EQUALS }
                )
                existing
            }
            else {
                if (closeBracket?.treeParent != optionalParam.node) {
                    Log.debug("Close bracket is not a child of the optional parameter for ${command.text}, name=$name, value=$value")
                    return null
                }
                val comma = createFromText(",").findFirstChildOfType(LatexNormalText::class)?.firstChild ?: return pair
                optionalParam.addBefore(comma, closeBracket)
                optionalParam.addBefore(pair, closeBracket)
                closeBracket.prevSibling as? LatexOptionalKeyValPair
            }
        }
        else {
            optionalParam.addBefore(pair, closeBracket)
            closeBracket?.prevSibling as? LatexOptionalKeyValPair
        }
    }

    /**
     * Create a [PsiWhiteSpace] element that contains the first spacing of the string [space].
     */
    fun createSpacing(space: String = " "): PsiWhiteSpace? = LatexPsiHelper(project)
        .createFromText(space)
        .findFirstChildOfType(PsiWhiteSpace::class)

    companion object{


        fun createFromText(text: String, project: Project): PsiFile {
            return PsiFileFactory.getInstance(project).createFileFromText("DUMMY.tex", LatexLanguage, text, false, true)
        }
    }
}