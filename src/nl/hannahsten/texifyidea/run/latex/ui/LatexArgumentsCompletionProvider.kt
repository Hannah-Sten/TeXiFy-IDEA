package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.externalSystem.service.execution.cmd.CommandLineCompletionProvider
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

/**
 * Simple way to add autocompletion to a command line editor.
 * Based on MavenArgumentsCompletionProvider used in MavenBeforeRunTasksProvider
 * Note that there is a similar (and better) solution for fragments, see MavenRunConfigurationSettingsEditor#addCommandLineFragment
 */
class LatexArgumentsCompletionProvider : CommandLineCompletionProvider(options) {

    companion object {
        val options = Options().apply {
            // option is with one - and .longOpt is with two --
            addOption(Option.builder("etex").longOpt("etex").desc("enable e-TeX extensions").build())
        }
    }

    override fun addArgumentVariants(result: CompletionResultSet) {
        // Here we can add things to the autocompletion without the - or -- prefix, for example:
//        result.addAllElements(listOf("one", "two").map { LookupElementBuilder.create(it) })
    }
}
