package nl.hannahsten.texifyidea.grammar

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.file.BibtexFile
import nl.hannahsten.texifyidea.parser.BibtexParser
import nl.hannahsten.texifyidea.psi.BibtexTypes

/**
 * @author Hannah Schellekens
 */
class BibtexParserDefinition : ParserDefinition {

    override fun createLexer(project: Project) = BibtexLexerAdapter()

    override fun createParser(project: Project) = BibtexParser()

    override fun getFileNodeType(): IStubFileElementType<*> = FILE

    override fun getWhitespaceTokens(): TokenSet = BibtexTokenSets.WHITE_SPACES

    override fun getCommentTokens(): TokenSet = BibtexTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = BibtexTokenSets.NORMAL_TEXT

    override fun createElement(astNode: ASTNode): PsiElement = BibtexTypes.Factory.createElement(astNode)

    override fun createFile(fileViewProvider: FileViewProvider): PsiFile = BibtexFile(fileViewProvider)

    override fun spaceExistenceTypeBetweenTokens(
        astNode: ASTNode,
        astNode1: ASTNode
    ): SpaceRequirements = SpaceRequirements.MAY

    companion object {

        val FILE = object : IStubFileElementType<BibtexFileStub>(
            "BibtexStubFileElementType", Language.findInstance(BibtexLanguage::class.java)
        ) {
            override fun getStubVersion(): Int = 11
        }
    }

    class BibtexFileStub(file: BibtexFile) : PsiFileStubImpl<BibtexFile>(file)
}
