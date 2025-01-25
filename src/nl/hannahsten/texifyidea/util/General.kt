package nl.hannahsten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

/**
 * Returns `1` when `true`, returns `0` when `false`.
 */
val Boolean.int: Int
    get() = if (this) 1 else 0

// Copied from grazie utils
fun Boolean?.orTrue() = this ?: true
fun Boolean?.orFalse() = this ?: false

/**
 * Creates a pair of two objects, analogous to [to].
 */
infix fun <T1, T2> T1.and(other: T2) = Pair(this, other)

/**
 * Executes the given run write action.
 */
fun runWriteAction(writeAction: () -> Unit) {
    ApplicationManager.getApplication().runWriteAction(writeAction)
}

fun runWriteCommandAction(project: Project, writeCommandAction: () -> Unit) {
    WriteCommandAction.runWriteCommandAction(project, writeCommandAction)
}

fun <T> runWriteCommandAction(
    project: Project,
    commandName: String,
    vararg files: PsiFile,
    writeCommandAction: () -> T
): T {
    return WriteCommandAction.writeCommandAction(project, *files).withName(commandName)
        .compute<T, RuntimeException>(writeCommandAction)
}

/**
 * Converts an [IntRange] to [TextRange].
 */
fun IntRange.toTextRange() = TextRange(this.first, this.last + 1)

/**
 * Get the length of an [IntRange].
 */
val IntRange.length: Int
    get() = endInclusive - start

/**
 * Converts the range to a range representation with the given seperator.
 * When the range has size 0, it will only print the single number.
 */
fun IntRange.toRangeString(separator: String = "-") =
    if (start == endInclusive) start else "$start$separator$endInclusive"

/**
 * Shift the range to the right by the number of places given.
 */
fun IntRange.shiftRight(displacement: Int): IntRange {
    return (this.first + displacement)..(this.last + displacement)
}

/**
 * Converts a [TextRange] to [IntRange].
 */
fun TextRange.toIntRange() = startOffset until endOffset

/**
 * Easy access to [java.util.regex.Matcher.matches].
 */
fun Pattern.matches(sequence: CharSequence?) = if (sequence != null) matcher(sequence).matches() else false

/**
 * Use [runInBackground] instead
 */
@Deprecated("Use runInBackground, and convert all runReadAction to smartReadAction")
fun runInBackgroundBlocking(project: Project?, description: String, function: (indicator: ProgressIndicator) -> Unit) {
    ProgressManager.getInstance().run(object : Backgroundable(project, description) {
        override fun run(indicator: ProgressIndicator) {
            function(indicator)
        }
    })
}

/**
 * Use [runInBackground] if you have a meaningful coroutine scope.
 */
fun runInBackgroundNonBlocking(project: Project, description: String, function: suspend (ProgressReporter) -> Unit) {
    // We don't need to block until it finished
    CoroutineScope(Dispatchers.IO).launch {
        runInBackground(project, description, function)
    }
}

suspend fun runInBackground(project: Project, description: String, function: suspend (ProgressReporter) -> Unit) = withContext(Dispatchers.IO) {
    // We don't need to suspend and wait for the result
    launch {
        withBackgroundProgress(project, description) {
            // Work size only allows integers, but we don't know the size here yet, so we start at 100.0%
            reportProgress(size = 1000) { function(it) }
        }
    }
}

fun runInBackgroundWithoutProgress(function: () -> Unit) {
    ApplicationManager.getApplication().invokeLater {
        function()
    }
}
