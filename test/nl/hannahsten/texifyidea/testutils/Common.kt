package nl.hannahsten.texifyidea.testutils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project

/**
 * Execute the given action as write command.
 * Can be used e.g. for running inspection QuickFixes
 *
 * @see WriteCommandAction
 * @see WriteCommandAction.Simple
 */
fun <T> writeCommand(project: Project, action: () -> T) {
    WriteCommandAction.writeCommandAction(project).compute<T, Exception> {
        action.invoke()
    }
}