package nl.rubensten.texifyidea.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Ruben Schellekens
 */
open class BibtexSyntaxHighlighterFactory : SyntaxHighlighterFactory() {

    override fun getSyntaxHighlighter(project: Project?, file: VirtualFile?) = BibtexSyntaxHighlighter()
}