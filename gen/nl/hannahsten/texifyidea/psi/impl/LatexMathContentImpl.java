// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class LatexMathContentImpl extends ASTWrapperPsiElement implements LatexMathContent {

  public LatexMathContentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitMathContent(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexNoMathContent> getNoMathContentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexNoMathContent.class);
  }

}
