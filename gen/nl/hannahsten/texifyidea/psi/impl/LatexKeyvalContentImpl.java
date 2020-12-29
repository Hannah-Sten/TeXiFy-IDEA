// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.psi.LatexKeyvalContent;
import nl.hannahsten.texifyidea.psi.LatexKeyvalText;
import nl.hannahsten.texifyidea.psi.LatexParameterGroup;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LatexKeyvalContentImpl extends ASTWrapperPsiElement implements LatexKeyvalContent {

  public LatexKeyvalContentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitKeyvalContent(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexKeyvalText getKeyvalText() {
    return PsiTreeUtil.getChildOfType(this, LatexKeyvalText.class);
  }

  @Override
  @Nullable
  public LatexParameterGroup getParameterGroup() {
    return PsiTreeUtil.getChildOfType(this, LatexParameterGroup.class);
  }

}
