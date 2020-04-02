package nl.hannahsten.texifyidea.editor

import com.intellij.lang.Commenter

/**
 * @author Sten Wessel
 */
open class LatexCommenter : Commenter {

    override fun getLineCommentPrefix() = "%"

    override fun getBlockCommentPrefix(): String? = null

    override fun getBlockCommentSuffix(): String? = null

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}
