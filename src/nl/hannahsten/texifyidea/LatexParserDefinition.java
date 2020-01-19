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
import nl.hannahsten.texifyidea.file.LatexFile;
import nl.hannahsten.texifyidea.parser.LatexParser;
import nl.hannahsten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexParserDefinition implements ParserDefinition {

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(LatexTypes.COMMENT_TOKEN);
    public static final TokenSet NORMAL_TEXT = TokenSet.create(LatexTypes.NORMAL_TEXT);

    public static final IStubFileElementType FILE = new IStubFileElementType(
            Language.findInstance(LatexLanguage.class)
    );

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new LatexLexerAdapter();
    }

    @Override
    public PsiParser createParser(Project project) {
        return new LatexParser();
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
    public PsiElement createElement(ASTNode node) {
        return LatexTypes.Factory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new LatexFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
