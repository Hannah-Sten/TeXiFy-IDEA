// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import nl.hannahsten.texifyidea.index.stub.LatexMagicCommentStub;
import nl.hannahsten.texifyidea.psi.LatexMagicComment;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;

import static nl.hannahsten.texifyidea.psi.LatexTypes.MAGIC_COMMENT_TOKEN;

public class LatexMagicCommentImpl extends StubBasedPsiElementBase<LatexMagicCommentStub> implements LatexMagicComment {

  public LatexMagicCommentImpl(@NotNull LatexMagicCommentStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public LatexMagicCommentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LatexMagicCommentImpl(LatexMagicCommentStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitMagicComment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getMagicCommentToken() {
    return notNullChild(findChildByType(MAGIC_COMMENT_TOKEN));
  }

}
