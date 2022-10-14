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

public class BibtexStringImpl extends ASTWrapperPsiElement implements BibtexString {

  public BibtexStringImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitString(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BibtexVisitor) accept((BibtexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BibtexBracedString getBracedString() {
    return PsiTreeUtil.getChildOfType(this, BibtexBracedString.class);
  }

  @Override
  @Nullable
  public BibtexBracedVerbatim getBracedVerbatim() {
    return PsiTreeUtil.getChildOfType(this, BibtexBracedVerbatim.class);
  }

  @Override
  @Nullable
  public BibtexDefinedString getDefinedString() {
    return PsiTreeUtil.getChildOfType(this, BibtexDefinedString.class);
  }

  @Override
  @Nullable
  public BibtexQuotedString getQuotedString() {
    return PsiTreeUtil.getChildOfType(this, BibtexQuotedString.class);
  }

  @Override
  @Nullable
  public BibtexQuotedVerbatim getQuotedVerbatim() {
    return PsiTreeUtil.getChildOfType(this, BibtexQuotedVerbatim.class);
  }

}
