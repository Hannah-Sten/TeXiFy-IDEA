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
import com.intellij.psi.PsiReference;

public class BibtexTagImpl extends ASTWrapperPsiElement implements BibtexTag {

  public BibtexTagImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitTag(this);
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
  @Nullable
  public BibtexContent getContent() {
    return PsiTreeUtil.getChildOfType(this, BibtexContent.class);
  }

  @Override
  @NotNull
  public BibtexKey getKey() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, BibtexKey.class));
  }

  @Override
  public PsiReference[] getReferences() {
    return BibtexPsiImplUtil.getReferences(this);
  }

}
