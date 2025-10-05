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
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.parser.LatexParser
import nl.hannahsten.texifyidea.psi.LatexTypes
import org.jetbrains.annotations.NonNls

/**
 * @author Sten Wessel
 */
class LatexParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = LatexLexerAdapter()

    override fun createParser(project: Project): PsiParser = LatexParser()

    override fun getFileNodeType(): IStubFileElementType<*> = LatexStubFileElementType

    override fun getWhitespaceTokens(): TokenSet = LatexTokenSets.WHITE_SPACES

    override fun getCommentTokens(): TokenSet = LatexTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = LatexTokenSets.NORMAL_TEXT

    override fun createElement(node: ASTNode): PsiElement = LatexTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = LatexFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(
        left: ASTNode,
        right: ASTNode
    ): SpaceRequirements = SpaceRequirements.MAY
}

object LatexStubFileElementType : IStubFileElementType<LatexFileStub>(
    // debugName is required to let IntelliJ distinguish between this FILE and BibtexParserDefinition.FILE
    "LatexStubFileElementType", Language.findInstance(LatexLanguage::class.java)
) {
    override fun getStubVersion(): Int = 89

    override fun getExternalId(): @NonNls String {
        return "texify.latex.LatexStubFileElementType"
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): LatexFileStub {
        return LatexFileStub(null)
    }

    override fun serialize(stub: LatexFileStub, dataStream: StubOutputStream) {
        super.serialize(stub, dataStream)
    }
}

class LatexFileStub(file: LatexFile?) : PsiFileStubImpl<LatexFile>(file)