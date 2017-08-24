package nl.rubensten.texifyidea.editor

import com.intellij.lang.Commenter

/**
 * @author Sten Wessel
 */
open class LatexCommenter : Commenter {

    override fun getLineCommentPrefix(): String? = "%"

    override fun getBlockCommentPrefix(): String? = "(BLOCKPRE)"

    override fun getBlockCommentSuffix(): String? = "(BLOCKEND)"

    override fun getCommentedBlockCommentPrefix(): String? = "(CMDPRE)"

    override fun getCommentedBlockCommentSuffix(): String? = "(CMDEND)"
}
