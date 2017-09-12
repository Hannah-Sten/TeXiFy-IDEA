package nl.rubensten.texifyidea

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import nl.rubensten.texifyidea.file.BibtexFile
import nl.rubensten.texifyidea.parser.BibtexParser
import nl.rubensten.texifyidea.psi.BibtexTypes

/**
 * @author Ruben Schellekens
 */
open class BibtexParserDefinition : ParserDefinition {

    companion object {

        @JvmStatic
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)

        @JvmStatic
        val COMMENTS = TokenSet.create(BibtexTypes.COMMENT)

        @JvmStatic
        val FILE = IFileElementType(BibtexLanguage)
    }

    override fun createParser(project: Project?) = BibtexParser()

    override fun createLexer(project: Project) = BibtexLexerAdapter()

    override fun createFile(viewProvider: FileViewProvider) = BibtexFile(viewProvider)

    override fun spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode) = SpaceRequirements.MAY

    override fun createElement(node: ASTNode) = BibtexTypes.Factory.createElement(node)!!

    override fun getStringLiteralElements() = TokenSet.EMPTY!!

    override fun getWhitespaceTokens() = WHITE_SPACES

    override fun getCommentTokens() = COMMENTS

    override fun getFileNodeType() = FILE
}