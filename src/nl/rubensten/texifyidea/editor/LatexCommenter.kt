package nl.rubensten.texifyidea.editor

import com.intellij.lang.Commenter

/**
 * @author Sten Wessel
 */
open class LatexCommenter : Commenter {

    override fun getLineCommentPrefix(): String? = "%"

    override fun getBlockCommentPrefix(): String? = ""

    override fun getBlockCommentSuffix(): String? = ""

    override fun getCommentedBlockCommentPrefix(): String? = ""

    override fun getCommentedBlockCommentSuffix(): String? = ""
}
