// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class BibtexContentImpl extends ASTWrapperPsiElement implements BibtexContent {

  public BibtexContentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitContent(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BibtexVisitor) accept((BibtexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<BibtexString> getStringList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BibtexString.class);
  }

}
