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

public class LatexRequiredParamImpl extends ASTWrapperPsiElement implements LatexRequiredParam {

  public LatexRequiredParamImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitRequiredParam(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexRequiredParamContent> getRequiredParamContentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexRequiredParamContent.class);
  }

  @Override
  @NotNull
  public List<LatexStrictKeyValPair> getStrictKeyValPairList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexStrictKeyValPair.class);
  }

}
