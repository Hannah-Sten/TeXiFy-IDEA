// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi.impl;

import nl.rubensten.texifyidea.psi.LatexCommand;
import nl.rubensten.texifyidea.psi.LatexComment;
import nl.rubensten.texifyidea.psi.LatexGroup;
import nl.rubensten.texifyidea.psi.LatexNoMathContent;
import nl.rubensten.texifyidea.psi.LatexOpenGroup;
import nl.rubensten.texifyidea.psi.LatexTypes;
import nl.rubensten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;

public class LatexNoMathContentImpl extends ASTWrapperPsiElement implements LatexNoMathContent {

  public LatexNoMathContentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitNoMathContent(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LatexCommand getCommand() {
    return findChildByClass(LatexCommand.class);
  }

  @Override
  @Nullable
  public LatexComment getComment() {
    return findChildByClass(LatexComment.class);
  }

  @Override
  @Nullable
  public LatexGroup getGroup() {
    return findChildByClass(LatexGroup.class);
  }

  @Override
  @Nullable
  public LatexOpenGroup getOpenGroup() {
    return findChildByClass(LatexOpenGroup.class);
  }

  @Override
  @Nullable
  public PsiElement getNormalText() {
    return findChildByType(LatexTypes.NORMAL_TEXT);
  }

}
