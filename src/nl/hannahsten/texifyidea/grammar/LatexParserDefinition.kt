package nl.hannahsten.texifyidea.grammar

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.parser.LatexParser
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * @author Sten Wessel
 */
class LatexParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = LatexLexerAdapter()

    override fun createParser(project: Project): PsiParser = LatexParser()

    override fun getFileNodeType(): IStubFileElementType<*> = Cache.FILE

    override fun getWhitespaceTokens(): TokenSet = LatexTokenSets.WHITE_SPACES

    override fun getCommentTokens(): TokenSet = LatexTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = LatexTokenSets.NORMAL_TEXT

    override fun createElement(node: ASTNode): PsiElement = LatexTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = LatexFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(
        left: ASTNode,
        right: ASTNode
    ): SpaceRequirements = SpaceRequirements.MAY

    object Cache {
        // debugName is required to let IntelliJ distinguish between this FILE and BibtexParserDefinition.FILE
        val FILE: IStubFileElementType<*> = object : IStubFileElementType<LatexFileStub>(
            "LatexStubFileElementType", Language.findInstance(LatexLanguage::class.java)
        ) {
            override fun getStubVersion(): Int = 80
        }
    }

    class LatexFileStub(file: LatexFile) : PsiFileStubImpl<LatexFile>(file)
}