package nl.rubensten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.rubensten.texifyidea.algorithm.IsChildDFS
import nl.rubensten.texifyidea.file.ClassFileType
import nl.rubensten.texifyidea.file.LatexFileType
import nl.rubensten.texifyidea.file.StyleFileType
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.lang.Package
import java.util.*
import java.util.regex.Pattern

/**
 * Remove all appearances of all given strings.
 */
fun String.removeAll(vararg strings: String): String {
    var formatted = this
    strings.forEach { formatted = formatted.replace(it, "") }
    return formatted
}

/**
 * Formats the string as a valid filename, removing not-allowed characters, in TeX-style with - as separator. // todo make sure this is used everywhere
 */
fun String.formatAsFileName(): String {
    val formatted = this.replace(" ", "-")
            .removeAll("/", "\\", "<", ">", "\"", "|", "?", "*", ":") // Mostly just a problem on Windows
            .toLowerCase()

    // If there are no valid characters left, use a default name.
    return if (formatted.isEmpty()) { "myfile" } else { formatted }
}

/**
 * Formats the string as a valid LaTeX label name.
 */
fun String.formatAsLabel(): String {
    return replace(" ", "-")
            .removeAll("%", "~", "#", "\\")
            .toLowerCase()
}