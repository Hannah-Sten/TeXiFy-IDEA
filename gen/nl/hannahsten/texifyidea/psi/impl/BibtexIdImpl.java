// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.psi.BibtexComment;
import nl.hannahsten.texifyidea.psi.BibtexId;
import nl.hannahsten.texifyidea.psi.BibtexPsiImplUtil;
import nl.hannahsten.texifyidea.psi.BibtexVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BibtexIdImpl extends ASTWrapperPsiElement implements BibtexId {

  public BibtexIdImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitId(this);
  }

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

}
