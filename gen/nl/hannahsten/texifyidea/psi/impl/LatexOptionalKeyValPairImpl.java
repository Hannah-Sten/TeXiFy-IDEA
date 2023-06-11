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

public class LatexOptionalKeyValPairImpl extends ASTWrapperPsiElement implements LatexOptionalKeyValPair {

  public LatexOptionalKeyValPairImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitOptionalKeyValPair(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexKeyValValue getKeyValValue() {
    return PsiTreeUtil.getChildOfType(this, LatexKeyValValue.class);
  }

  @Override
  @NotNull
  public LatexOptionalKeyValKey getOptionalKeyValKey() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LatexOptionalKeyValKey.class));
  }

  @Override
  public @NotNull LatexKeyValKey getKeyValKey() {
    return LatexPsiImplUtil.getKeyValKey(this);
  }

}
