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

public class LatexKeyValPairImpl extends ASTWrapperPsiElement implements LatexKeyValPair {

  public LatexKeyValPairImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitKeyValPair(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public LatexKeyValKey getKeyValKey() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LatexKeyValKey.class));
  }

  @Override
  @Nullable
  public LatexKeyValValue getKeyValValue() {
    return PsiTreeUtil.getChildOfType(this, LatexKeyValValue.class);
  }

}
