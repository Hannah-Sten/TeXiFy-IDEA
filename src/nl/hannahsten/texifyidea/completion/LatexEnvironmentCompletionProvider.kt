package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.Dependend
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.SimpleEnvironment
import nl.hannahsten.texifyidea.util.Kindness.getKindWords
import nl.hannahsten.texifyidea.util.magic.CommandMagic

object LatexEnvironmentCompletionProvider : CompletionProvider<CompletionParameters>() {


    private val defaultLookupElements = DefaultEnvironment.entries.map(::createEnvironmentLookupElement)

    private fun packageName(dependend: Dependend): String {
        val name = dependend.dependency.name
        return if ("" == name) {
            ""
        }
        else " ($name)"
    }

    private fun createEnvironmentLookupElement(env: Environment): LookupElementBuilder {
        return LookupElementBuilder.create(env, env.environmentName)
            .withPresentableText(env.environmentName)
            .bold()
            .withTailText(env.getArgumentsDisplay() + " " + packageName(env), true)
            .withIcon(TexifyIcons.DOT_ENVIRONMENT)
    }

    fun addStubIndexCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val fileset = LatexProjectStructure.buildFilesetScope(parameters.originalFile)
        val lookups = NewSpecialCommandsIndex.getAllEnvDef(fileset)
            .asSequence()
            .filter { cmd -> CommandMagic.environmentDefinitions.contains(cmd.name) }
            .mapNotNull { cmd -> cmd.requiredParameterText(0) }
            .map { environmentName -> createEnvironmentLookupElement(SimpleEnvironment(environmentName)) }
            .toList()
        // Create autocomplete elements.
        result.addAllElements(lookups)

    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        result.addAllElements(defaultLookupElements)
        addStubIndexCompletions(parameters, context, result)
        result.addLookupAdvertisement(getKindWords())
    }
}