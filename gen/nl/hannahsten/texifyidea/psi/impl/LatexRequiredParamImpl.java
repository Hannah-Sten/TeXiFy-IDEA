// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class LatexRequiredParamImpl extends ASTWrapperPsiElement implements LatexRequiredParam {

  public LatexRequiredParamImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitRequiredParam(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public LatexGroup getGroup() {
    return findNotNullChildByClass(LatexGroup.class);
  }

}
