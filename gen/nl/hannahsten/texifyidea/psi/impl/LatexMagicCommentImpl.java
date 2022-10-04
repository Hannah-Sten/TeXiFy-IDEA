// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import nl.hannahsten.texifyidea.index.stub.LatexMagicCommentStub;
import nl.hannahsten.texifyidea.psi.*;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

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
