// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.psi.LatexKeyvalContent;
import nl.hannahsten.texifyidea.psi.LatexKeyvalValue;
import nl.hannahsten.texifyidea.psi.LatexPsiImplUtil;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LatexKeyvalValueImpl extends ASTWrapperPsiElement implements LatexKeyvalValue {

  public LatexKeyvalValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitKeyvalValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexKeyvalContent> getKeyvalContentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexKeyvalContent.class);
  }

  @Override
  public String toString() {
    return LatexPsiImplUtil.toString(this);
  }

}
