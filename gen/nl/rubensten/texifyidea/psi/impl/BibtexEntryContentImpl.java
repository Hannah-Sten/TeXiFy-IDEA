// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.rubensten.texifyidea.psi.BibtexTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.rubensten.texifyidea.psi.*;

public class BibtexEntryContentImpl extends ASTWrapperPsiElement implements BibtexEntryContent {

  public BibtexEntryContentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitEntryContent(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BibtexVisitor) accept((BibtexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BibtexId getId() {
    return findChildByClass(BibtexId.class);
  }

  @Override
  @NotNull
  public List<BibtexTag> getTagList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BibtexTag.class);
  }

}
