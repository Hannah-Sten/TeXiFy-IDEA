package nl.hannahsten.texifyidea.lang

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.index.file.LatexExternalEnvironmentIndex
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.util.files.removeFileExtension
import nl.hannahsten.texifyidea.util.startsWithAny

/**
 * @author Hannah Schellekens
 */
interface Environment : Dependend, Described {

    companion object {

        /**
         * Looks up a default environment by the given name.
         *
         * @param environmentName
         *              The name of the environment object to get.
         * @return The [DefaultEnvironment] with the given name, or `null` when it couldn't
         * be found.
         */
        fun lookup(environmentName: String) = DefaultEnvironment[environmentName]

        /**
         * Create an [Environment] for the given environment name.
         * See [LatexCommand.lookupInIndex].
         */
        fun lookupInIndex(environmentName: String, project: Project): Set<Environment> {
            val envs = mutableSetOf<Environment>()
            FileBasedIndex.getInstance().processValues(
                LatexExternalEnvironmentIndex.id, environmentName, null, { file, value ->
                    val dependency = file.name.removeFileExtension()
                    val env = object : Environment {
                        override val arguments = extractArgumentsFromDocs(value)
                        override val description = value
                        override val dependency =
                            if (dependency.isBlank()) LatexPackage.DEFAULT else LatexPackage.create(file)
                        override val context = Context.NORMAL
                        override val initialContents = ""
                        override val environmentName = environmentName
                    }
                    envs.add(env)
                    true
                },
                GlobalSearchScope.everythingScope(project)
            )

            // See LatexCommand#lookUpInIndex()
            return envs.distinctBy { listOf(it.environmentName, it.dependency, it.context, it.description).plus(it.arguments) }
                .groupBy { listOf(it.environmentName, it.dependency, it.context) }
                .mapValues { it.value.maxByOrNull { cmd -> cmd.description }!! }
                .values.toSet()
        }

        fun extractArgumentsFromDocs(docs: String): Array<Argument> {
            // Maybe the arguments are given right at the beginning of the docs
            val argCommands = arrayOf(OARG, MARG, PARG).map { it.commandWithSlash }.toTypedArray()
            if (docs.startsWithAny(*argCommands)) {
                return LatexCommand.getArgumentsFromStartOfString(docs, 0)
            }

            return emptyArray()
        }

        /**
         * @see [lookup]
         */
        operator fun get(environmentName: String) = lookup(environmentName)
    }

    /**
     * Get what type of context this enviroment has inside.
     */
    val context: Context

    /**
     * Get the contents that must be placed into the environment just after it has been
     * inserted using the auto complete.
     */
    val initialContents: String

    /**
     * Get the name of the environment.
     */
    val environmentName: String

    /**
     * Get all the environment myArguments.
     */
    val arguments: Array<out Argument>

    /**
     * Concatenates all arguments to each other.
     *
     * @return e.g. `{ARG1}{ARG2}[ARG3]`
     */
    @Suppress("KDocUnresolvedReference")
    fun getArgumentsDisplay(): String {
        val sb = StringBuilder()
        for (arg in arguments) {
            sb.append(arg.toString())
        }

        return sb.toString()
    }

    /**
     * @author Hannah Schellekens
     */
    enum class Context {

        NORMAL,
        MATH,
        COMMENT
    }
}
