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

public class LatexInlineMathImpl extends ASTWrapperPsiElement implements LatexInlineMath {

  public LatexInlineMathImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitInlineMath(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexMathContent getMathContent() {
    return PsiTreeUtil.getChildOfType(this, LatexMathContent.class);
  }

  @Override
  @Nullable
  public PsiElement getInlineMathEnd() {
    return findChildByType(INLINE_MATH_END);
  }

  @Override
  @NotNull
  public PsiElement getInlineMathStart() {
    return notNullChild(findChildByType(INLINE_MATH_START));
  }

}
