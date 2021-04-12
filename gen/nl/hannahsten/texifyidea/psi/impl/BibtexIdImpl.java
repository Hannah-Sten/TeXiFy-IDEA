// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.BibtexTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class BibtexIdImpl extends ASTWrapperPsiElement implements BibtexId {

  public BibtexIdImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitId(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BibtexVisitor) accept((BibtexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<BibtexComment> getCommentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BibtexComment.class);
  }

  @Override
  public PsiElement getNameIdentifier() {
    return BibtexPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public String getName() {
    return BibtexPsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(String name) {
    return BibtexPsiImplUtil.setName(this, name);
  }

  @Override
  public void delete() {
    BibtexPsiImplUtil.delete(this);
  }

}
