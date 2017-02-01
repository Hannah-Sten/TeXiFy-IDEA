// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexParameter;
import nl.rubensten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static nl.rubensten.texifyidea.psi.LatexTypes.COMMAND_TOKEN;

public class LatexCommandsImpl extends ASTWrapperPsiElement implements LatexCommands {

  public LatexCommandsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitCommands(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexParameter.class);
  }

  @Override
  @NotNull
  public PsiElement getCommandToken() {
    return findNotNullChildByType(COMMAND_TOKEN);
  }

}
