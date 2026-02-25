package nl.hannahsten.texifyidea.run.common

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.impl.ExecutionManagerImpl
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.util.Log
import java.nio.file.Path
import kotlin.io.path.exists

internal data class ProcessContext(
    val environment: ExecutionEnvironment,
    val mainFile: VirtualFile,
    val command: List<String>,
    val workingDirectory: Path,
    val expandMacrosEnvVariables: Boolean,
    val envs: Map<String, String>,
    val expandEnvValue: (String) -> String,
)

@Throws(ExecutionException::class)
internal fun createCompilationHandler(
    environment: ExecutionEnvironment,
    mainFile: VirtualFile,
    command: List<String>,
    workingDirectory: Path?,
    expandMacrosEnvVariables: Boolean,
    envs: Map<String, String>,
    expandEnvValue: (String) -> String,
): KillableProcessHandler {
    val context = ProcessContext(
        environment = environment,
        mainFile = mainFile,
        command = command,
        workingDirectory = workingDirectory ?: Path.of(mainFile.parent.path),
        expandMacrosEnvVariables = expandMacrosEnvVariables,
        envs = envs,
        expandEnvValue = expandEnvValue,
    )

    if (context.workingDirectory.exists().not()) {
        Notification(
            "LaTeX",
            "Could not find working directory",
            "The directory containing the main file could not be found: ${context.workingDirectory}",
            NotificationType.ERROR,
        ).notify(context.environment.project)
        throw ExecutionException("Could not find working directory ${context.workingDirectory} for file ${context.mainFile}")
    }

    val envVariables = if (!context.expandMacrosEnvVariables) {
        context.envs
    }
    else {
        ExecutionManagerImpl.withEnvironmentDataContext(
            SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, context.mainFile, context.environment.dataContext),
        ).use {
            context.envs.mapValues { context.expandEnvValue(it.value) }
        }
    }

    if (SystemInfo.isWindows && context.command.sumOf { it.length } > 10_000) {
        throw ExecutionException("The following command was too long to run: ${context.command.joinToString(" ")}")
    }

    val commandLine = GeneralCommandLine(context.command)
        .withWorkingDirectory(context.workingDirectory)
        .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        .withEnvironment(envVariables)

    Log.debug("Executing ${commandLine.commandLineString} in ${context.workingDirectory}")

    val handler = KillableProcessHandler(commandLine)
    ProcessTerminatedListener.attach(handler, context.environment.project)

    return handler
}
