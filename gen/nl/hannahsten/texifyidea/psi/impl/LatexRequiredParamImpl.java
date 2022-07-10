// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.psi.LatexRequiredParam;
import nl.hannahsten.texifyidea.psi.LatexRequiredParamContent;
import nl.hannahsten.texifyidea.psi.LatexStrictKeyvalPair;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LatexRequiredParamImpl extends ASTWrapperPsiElement implements LatexRequiredParam {

  public LatexRequiredParamImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitRequiredParam(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexRequiredParamContent> getRequiredParamContentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexRequiredParamContent.class);
  }

  @Override
  @NotNull
  public List<LatexStrictKeyvalPair> getStrictKeyvalPairList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexStrictKeyvalPair.class);
  }

}
