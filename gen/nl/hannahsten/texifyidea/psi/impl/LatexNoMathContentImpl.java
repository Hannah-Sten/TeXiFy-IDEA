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

public class LatexNoMathContentImpl extends ASTWrapperPsiElement implements LatexNoMathContent {

  public LatexNoMathContentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitNoMathContent(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexCommands getCommands() {
    return PsiTreeUtil.getChildOfType(this, LatexCommands.class);
  }

  @Override
  @Nullable
  public LatexComment getComment() {
    return PsiTreeUtil.getChildOfType(this, LatexComment.class);
  }

  @Override
  @Nullable
  public LatexEnvironment getEnvironment() {
    return PsiTreeUtil.getChildOfType(this, LatexEnvironment.class);
  }

  @Override
  @Nullable
  public LatexGroup getGroup() {
    return PsiTreeUtil.getChildOfType(this, LatexGroup.class);
  }

  @Override
  @Nullable
  public LatexMagicComment getMagicComment() {
    return PsiTreeUtil.getChildOfType(this, LatexMagicComment.class);
  }

  @Override
  @Nullable
  public LatexMathEnvironment getMathEnvironment() {
    return PsiTreeUtil.getChildOfType(this, LatexMathEnvironment.class);
  }

  @Override
  @Nullable
  public LatexNormalText getNormalText() {
    return PsiTreeUtil.getChildOfType(this, LatexNormalText.class);
  }

  @Override
  @Nullable
  public LatexPseudocodeBlock getPseudocodeBlock() {
    return PsiTreeUtil.getChildOfType(this, LatexPseudocodeBlock.class);
  }

  @Override
  @Nullable
  public LatexRawText getRawText() {
    return PsiTreeUtil.getChildOfType(this, LatexRawText.class);
  }

}
