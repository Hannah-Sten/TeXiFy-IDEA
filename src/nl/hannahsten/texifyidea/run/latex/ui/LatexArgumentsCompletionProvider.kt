package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.externalSystem.service.execution.cmd.CommandLineCompletionProvider
import org.apache.commons.cli.Options

/**
 * Simple way to add autocompletion to a command line editor.
 * Based on MavenArgumentsCompletionProvider used in MavenBeforeRunTasksProvider.
 */
class LatexArgumentsCompletionProvider(options: Options) : CommandLineCompletionProvider(options) {

    override fun addArgumentVariants(result: CompletionResultSet) {
        // Reserved for adding non-option completions.
    }
}
