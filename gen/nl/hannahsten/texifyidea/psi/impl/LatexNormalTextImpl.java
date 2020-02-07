// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import nl.hannahsten.texifyidea.psi.LatexNormalText;
import nl.hannahsten.texifyidea.psi.LatexPsiImplUtil;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;

public class LatexNormalTextImpl extends ASTWrapperPsiElement implements LatexNormalText {

  public LatexNormalTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitNormalText(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public PsiReference[] getReferences() {
    return LatexPsiImplUtil.getReferences(this);
  }

  @Override
  public PsiElement getNameIdentifier() {
    return LatexPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public String getName() {
    return LatexPsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(String name) {
    return LatexPsiImplUtil.setName(this, name);
  }

}
