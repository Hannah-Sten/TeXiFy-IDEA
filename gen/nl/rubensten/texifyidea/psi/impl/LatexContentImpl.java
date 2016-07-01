// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi.impl;

import nl.rubensten.texifyidea.psi.LatexContent;
import nl.rubensten.texifyidea.psi.LatexMathEnvironment;
import nl.rubensten.texifyidea.psi.LatexNoMathContent;
import nl.rubensten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;

public class LatexContentImpl extends ASTWrapperPsiElement implements LatexContent {

  public LatexContentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitContent(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexMathEnvironment getMathEnvironment() {
    return findChildByClass(LatexMathEnvironment.class);
  }

  @Override
  @Nullable
  public LatexNoMathContent getNoMathContent() {
    return findChildByClass(LatexNoMathContent.class);
  }

}
