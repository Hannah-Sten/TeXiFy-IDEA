// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import nl.hannahsten.texifyidea.psi.LatexKeyvalText;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;

public class LatexKeyvalTextImpl extends ASTWrapperPsiElement implements LatexKeyvalText {

  public LatexKeyvalTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitKeyvalText(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor) visitor);
    else super.accept(visitor);
  }

}
