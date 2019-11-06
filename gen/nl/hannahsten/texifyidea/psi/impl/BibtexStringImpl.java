// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.hannahsten.texifyidea.psi.*;

public class BibtexStringImpl extends ASTWrapperPsiElement implements BibtexString {

  public BibtexStringImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitString(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BibtexVisitor) accept((BibtexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BibtexBracedString getBracedString() {
    return findChildByClass(BibtexBracedString.class);
  }

  @Override
  @Nullable
  public BibtexDefinedString getDefinedString() {
    return findChildByClass(BibtexDefinedString.class);
  }

  @Override
  @Nullable
  public BibtexQuotedString getQuotedString() {
    return findChildByClass(BibtexQuotedString.class);
  }

}
