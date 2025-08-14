package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.SourcedDefinition
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticEnv

object ContextAwareEnvironmentCompletionProvider : LatexContextAwareCompletionAdaptor() {

    private fun buildEnvironmentSignature(env: LSemanticEnv): String = buildString {
        env.arguments.joinTo(this)
        append(' ')
        append(env.contextSignature.displayString())
    }

    private fun createEnvironmentLookupElement(
        env: LSemanticEnv, sourced: SourcedDefinition
    ): LookupElementBuilder {
        // somehow we have to add the \begin{ to the lookup string,
        // because the \begin{} command is recognized as a whole since we enable it to have references
        // See: LatexBeginCommandImplMixin,
        //
        val lookupString = env.name
        val tailText = buildEnvironmentSignature(env) + buildApplicableContextStr(env)
        return LookupElementBuilder.create(env, lookupString)
            .withPresentableText(env.name)
            .bold()
            .withTailText(tailText, true)
            .withTypeText(buildCommandSourceStr(sourced))
            .withIcon(TexifyIcons.DOT_ENVIRONMENT)
    }

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet) {
        val lookupElements = mutableListOf<LookupElementBuilder>()
        for (sd in defBundle.sourcedDefinitions()) {
            val env = sd.entity as? LSemanticEnv ?: continue
            if (!env.isApplicableIn(contexts)) continue
            lookupElements.add(createEnvironmentLookupElement(env, sd))
        }
        result.addAllElements(lookupElements)
    }
}