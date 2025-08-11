package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.SourcedEnvDefinition
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticEnv

object LatexContextAwareEnvironmentCompletionProvider : LatexContextAwareCompletionProviderBase() {


    private fun buildEnvironmentSignature(env: LSemanticEnv): String {
        return "${env.arguments.joinToString()} <${env.contextSignature}>"
    }

    private fun createEnvironmentLookupElement(sourced: SourcedEnvDefinition): LookupElementBuilder {
        // somehow we have to add the \begin{ to the lookup string,
        // because the \begin{} command is recognized as a whole since we enable it to have references
        // See: LatexBeginCommandImplMixin,
        //
        val env = sourced.entity
        val lookupString = env.name
        return LookupElementBuilder.create(env, lookupString)
            .withPresentableText(env.name)
            .bold()
            .withTailText(buildEnvironmentSignature(env), true)
            .withTypeText(buildCommandSourceStr(sourced))
            .withIcon(TexifyIcons.DOT_ENVIRONMENT)
    }

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, processingContext: ProcessingContext, result: CompletionResultSet) {
        val lookupElements = mutableListOf<LookupElementBuilder>()
        for (sd in defBundle.sourcedDefinitions()) {
            if (sd !is SourcedEnvDefinition) continue
            val env = sd.entity
            if (!contexts.containsAll(env.requiredContext)) continue
            lookupElements.add(createEnvironmentLookupElement(sd))
        }
        result.addAllElements(lookupElements)
    }
}