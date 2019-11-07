// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class LatexMathEnvironmentImpl extends ASTWrapperPsiElement implements LatexMathEnvironment {

  public LatexMathEnvironmentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitMathEnvironment(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexDisplayMath getDisplayMath() {
    return findChildByClass(LatexDisplayMath.class);
  }

  @Override
  @Nullable
  public LatexInlineMath getInlineMath() {
    return findChildByClass(LatexInlineMath.class);
  }

}
