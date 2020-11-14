// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class LatexMagicCommentImpl extends ASTWrapperPsiElement implements LatexMagicComment {

  public LatexMagicCommentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitMagicComment(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexNormalText> getNormalTextList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexNormalText.class);
  }

  @Override
  @Nullable
  public PsiElement getMagicCommentValue() {
    return findChildByType(MAGIC_COMMENT_VALUE);
  }

}
