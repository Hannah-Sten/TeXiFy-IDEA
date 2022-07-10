// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.psi.LatexGroup;
import nl.hannahsten.texifyidea.psi.LatexParameterGroupText;
import nl.hannahsten.texifyidea.psi.LatexParameterText;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LatexParameterGroupTextImpl extends ASTWrapperPsiElement implements LatexParameterGroupText {

  public LatexParameterGroupTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitParameterGroupText(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexGroup> getGroupList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexGroup.class);
  }

  @Override
  @NotNull
  public List<LatexParameterText> getParameterTextList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexParameterText.class);
  }

}
