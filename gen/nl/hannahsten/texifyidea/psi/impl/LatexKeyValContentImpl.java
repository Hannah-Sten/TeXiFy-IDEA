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

public class LatexKeyValContentImpl extends ASTWrapperPsiElement implements LatexKeyValContent {

  public LatexKeyValContentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitKeyValContent(this);
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
  public LatexMathEnvironment getMathEnvironment() {
    return PsiTreeUtil.getChildOfType(this, LatexMathEnvironment.class);
  }

  @Override
  @Nullable
  public LatexParameterGroup getParameterGroup() {
    return PsiTreeUtil.getChildOfType(this, LatexParameterGroup.class);
  }

  @Override
  @Nullable
  public LatexParameterText getParameterText() {
    return PsiTreeUtil.getChildOfType(this, LatexParameterText.class);
  }

}
