// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.BibtexTypes.*;
import nl.hannahsten.texifyidea.psi.*;

public class BibtexDefinedStringImpl extends BibtexDefinedStringImplMixin implements BibtexDefinedString {

  public BibtexDefinedStringImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitDefinedString(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BibtexVisitor) accept((BibtexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public BibtexKey getKey() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, BibtexKey.class));
  }

}
