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
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;

public class LatexParameterImpl extends ASTWrapperPsiElement implements LatexParameter {

  public LatexParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexOptionalParam getOptionalParam() {
    return PsiTreeUtil.getChildOfType(this, LatexOptionalParam.class);
  }

  @Override
  @Nullable
  public LatexPictureParam getPictureParam() {
    return PsiTreeUtil.getChildOfType(this, LatexPictureParam.class);
  }

  @Override
  @Nullable
  public LatexRequiredParam getRequiredParam() {
    return PsiTreeUtil.getChildOfType(this, LatexRequiredParam.class);
  }

  @Override
  public boolean isValidHost() {
    return LatexPsiImplUtil.isValidHost(this);
  }

  @Override
  public PsiLanguageInjectionHost updateText(@NotNull String text) {
    return LatexPsiImplUtil.updateText(this, text);
  }

  @Override
  public @NotNull LiteralTextEscaper<LatexParameter> createLiteralTextEscaper() {
    return LatexPsiImplUtil.createLiteralTextEscaper(this);
  }

}
