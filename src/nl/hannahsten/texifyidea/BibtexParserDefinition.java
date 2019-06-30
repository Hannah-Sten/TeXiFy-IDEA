package nl.hannahsten.texifyidea;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import nl.hannahsten.texifyidea.file.BibtexFile;
import nl.hannahsten.texifyidea.parser.BibtexParser;
import nl.hannahsten.texifyidea.psi.BibtexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Hannah Schellekens
 */
public class BibtexParserDefinition implements ParserDefinition {

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(BibtexTypes.COMMENT);
    public static final TokenSet NORMAL_TEXT = TokenSet.create(BibtexTypes.NORMAL_TEXT);

    public static final IStubFileElementType FILE = new IStubFileElementType(
            Language.findInstance(BibtexLanguage.class)
    );

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new BibtexLexerAdapter();
    }

    @Override
    public PsiParser createParser(Project project) {
        return new BibtexParser();
    }

    @Override
    public IStubFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return NORMAL_TEXT;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode astNode) {
        return BibtexTypes.Factory.createElement(astNode);
    }

    @Override
    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new BibtexFile(fileViewProvider);
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1) {
        return SpaceRequirements.MAY;
    }
}
