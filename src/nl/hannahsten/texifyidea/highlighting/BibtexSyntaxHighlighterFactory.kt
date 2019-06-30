package nl.hannahsten.texifyidea.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Hannah Schellekens
 */
open class BibtexSyntaxHighlighterFactory : SyntaxHighlighterFactory() {

    override fun getSyntaxHighlighter(project: Project?, file: VirtualFile?) = BibtexSyntaxHighlighter()
}