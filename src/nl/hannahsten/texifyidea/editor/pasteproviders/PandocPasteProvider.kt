package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.openapi.actionSystem.DataContext
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import org.jsoup.nodes.Node
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * todo
 */
class PandocPasteProvider(val isStandalone: Boolean = false) : LatexPasteProvider {

    val isPandocInPath: Boolean by lazy {
        "pandoc -v".runCommandWithExitCode().second == 0
    }

    override fun translateHtml(htmlIn: Node, dataContext: DataContext): String {

        return if (isPandocInPath) {
            val commands = arrayOf(
                "pandoc",
                "-f",
                "html",
                "-t",
                "latex"
            ) + if (isStandalone) arrayOf("--standalone") else arrayOf()
            Log.debug("Executing in ${GeneralCommandLine().commandLineString}")
            try {
                val proc = GeneralCommandLine(*commands)
                    .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                    .withWorkDirectory(null as File?)
                    .createProcess()

                val bufferedWriter = proc.outputStream.bufferedWriter()
                bufferedWriter.write(htmlIn)
                bufferedWriter.close()

                if (proc.waitFor(3, TimeUnit.SECONDS)) {
                    val output = proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader()
                        .readText().trim()
                    Log.debug("${commands.firstOrNull()} exited with ${proc.exitValue()} ${output.take(100)}")
                    return sanitizeOutput(output, isStandalone)
                }
                else {
                    val output = proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader()
                        .readText().trim()
                    proc.destroy()
                    proc.waitFor()
                    Log.debug("${commands.firstOrNull()} exited ${proc.exitValue()} with timeout")
                    return sanitizeOutput(output, isStandalone)
                }
            }
            catch (e: IOException) {
                Log.debug(e.message ?: "Unknown IOException occurred")
                return null
            }
            catch (e: ProcessNotCreatedException) {
                Log.debug(e.message ?: "Unknown ProcessNotCreatedException occurred")
                return null
            }
        }
        else null
    }

    private fun sanitizeOutput(rawOutput: String, hasDefinitions: Boolean = false): String {
        return if (!hasDefinitions)
            rawOutput
        else {
            rawOutput.replace("\\end{document}", "").replace("\\\\documentclass\\[\\s*]\\{article}".toRegex(), "")
        }
    }
}