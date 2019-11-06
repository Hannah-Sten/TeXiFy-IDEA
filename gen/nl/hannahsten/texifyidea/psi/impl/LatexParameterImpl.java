// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class LatexParameterImpl extends ASTWrapperPsiElement implements LatexParameter {

  public LatexParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitParameter(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexOptionalParam getOptionalParam() {
    return findChildByClass(LatexOptionalParam.class);
  }

  @Override
  @Nullable
  public LatexRequiredParam getRequiredParam() {
    return findChildByClass(LatexRequiredParam.class);
  }

}
