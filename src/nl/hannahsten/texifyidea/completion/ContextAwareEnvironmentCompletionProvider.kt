package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexAddImportInsertHandler
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexLibraryDefinitionService
import nl.hannahsten.texifyidea.index.SourcedDefinition
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation

object ContextAwareEnvironmentCompletionProvider : LatexContextAwareCompletionAdaptor() {

    private fun buildEnvironmentSignature(env: LSemanticEnv): String = buildString {
        env.arguments.joinTo(this, separator = "")
        append(' ')
        append(env.contextSignature.displayString())
    }

    private fun createEnvironmentLookupElement(
        env: LSemanticEnv, sourced: SourcedDefinition
    ): LookupElement {
        val tailText = buildEnvironmentSignature(env) + buildApplicableContextStr(env)
        return SimpleWithDefLookupElement.create(
            sourced, env.name,
            bold = true,
            tailText = tailText, tailTextGrayed = true,
            typeText = buildCommandSourceStr(sourced),
            icon = TexifyIcons.DOT_ENVIRONMENT,
            insertHandler = LatexAddImportInsertHandler
        )
    }

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet) {
        val completionMode = TexifySettings.getState().completionMode
        addBundleEnvironments(
            result, defBundle,
            checkCtx = completionMode == TexifySettings.CompletionMode.SMART, contexts = contexts
        )
        if (completionMode == TexifySettings.CompletionMode.ALL_PACKAGES) {
            addAllExternalEnvironments(parameters, result)
        }
    }

    private fun addBundleEnvironments(
        result: CompletionResultSet, defBundle: DefinitionBundle,
        checkCtx: Boolean = true, contexts: LContextSet = emptySet()
    ) {
        val lookupElements = mutableListOf<LookupElement>()
        for (sd in defBundle.sourcedDefinitions()) {
            val env = sd.entity as? LSemanticEnv ?: continue
            if (checkCtx && !env.isApplicableIn(contexts)) continue
            lookupElements.add(createEnvironmentLookupElement(env, sd))
        }
        result.addAllElements(lookupElements)
    }

    private fun addAllExternalEnvironments(
        parameters: CompletionParameters, result: CompletionResultSet
    ) {
        val project = parameters.originalFile.project
        val addedLibs = mutableSetOf<LatexLib>()
        val allNames = LatexPackageLocation.getAllPackageNames(project)
        val defService = LatexLibraryDefinitionService.getInstance(project)
        for (name in allNames) {
            val lib = LatexLib(name)
            if (!addedLibs.add(lib)) continue // skip already added libs
            val libBundle = defService.getLibBundle(name)
            addBundleEnvironments(result, libBundle, checkCtx = false)
            addedLibs.addAll(libBundle.allLibraries)
        }
    }
}