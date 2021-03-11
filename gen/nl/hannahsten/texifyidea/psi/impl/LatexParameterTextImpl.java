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
import com.intellij.psi.PsiReference;

public class LatexParameterTextImpl extends ASTWrapperPsiElement implements LatexParameterText {

  public LatexParameterTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitParameterText(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexCommands> getCommandsList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexCommands.class);
  }

  @Override
  public PsiReference[] getReferences() {
    return LatexPsiImplUtil.getReferences(this);
  }

  @Override
  public PsiReference getReference() {
    return LatexPsiImplUtil.getReference(this);
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

  @Override
  public void delete() {
    LatexPsiImplUtil.delete(this);
  }

}
