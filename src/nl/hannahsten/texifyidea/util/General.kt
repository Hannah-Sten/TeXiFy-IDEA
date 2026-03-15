package nl.hannahsten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiFile
import java.util.regex.Pattern

/**
 * Returns `1` when `true`, returns `0` when `false`.
 */
val Boolean.int: Int
    get() = if (this) 1 else 0

// Copied from grazie utils
@Suppress("unused")
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
): T = WriteCommandAction.writeCommandAction(project, *files).withName(commandName)
    .compute<T, RuntimeException>(writeCommandAction)

/**
 * Converts an [IntRange] to [TextRange].
 */
fun IntRange.toTextRange() = TextRange(this.first, this.last + 1)

/**
 * The length of an [IntRange], `length = endInclusive - start + 1`.
 */
val IntRange.length: Int
    get() = endInclusive - start + 1

fun IntRange.contains(other: IntRange): Boolean = this.first <= other.first && this.last >= other.last

/**
 * Converts the range to a range representation with the given seperator.
 * When the range has size 0, it will only print the single number.
 */
fun IntRange.toRangeString(separator: String = "-") =
    if (start == endInclusive) start else "$start$separator$endInclusive"

/**
 * Shift the range to the right by the number of places given.
 */
fun IntRange.shiftRight(displacement: Int): IntRange = (this.first + displacement)..(this.last + displacement)

/**
 * Converts a [TextRange] to [IntRange].
 */
fun TextRange.toIntRange() = startOffset until endOffset

/**
 * Easy access to [java.util.regex.Matcher.matches].
 */
fun Pattern.matches(sequence: CharSequence?) = if (sequence != null) matcher(sequence).matches() else false

const val PROGRESS_SIZE = 1000

/**
 * Runs the given function in a background thread, with a fake progress indicator, using [TexifyCoroutine.coroutineScope].
 *
 * IMPORTANT: Do not use runReadAction in the function, this may block the UI.
 * Use smartReadAction instead.
 *
 * See [coroutine-read-actions-api](https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html#coroutine-read-actions-api).
 *
 * We should use suspend functions like
 * [com.intellij.openapi.application.readAction], [com.intellij.openapi.application.smartReadAction]
 */
@Suppress("unused")
internal fun runInBackgroundNonBlocking(project: Project, description: String, function: suspend (ProgressReporter) -> Unit) {
    // We don't need to block until it finished
    TexifyCoroutine.runInBackground {
        withBackgroundProgress(project, description) {
            reportProgress(size = PROGRESS_SIZE) { function(it) }
        }
    }
}

// https://plugins.jetbrains.com/docs/intellij/background-processes.html
fun runInBackgroundWithoutProgress(function: suspend () -> Unit) {
    TexifyCoroutine.runInBackground {
        function.invoke()
    }
}
