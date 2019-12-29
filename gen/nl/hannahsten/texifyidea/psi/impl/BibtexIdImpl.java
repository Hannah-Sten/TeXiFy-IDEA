// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.BibtexTypes.*;
import nl.hannahsten.texifyidea.psi.BibtexIdImplMixin;
import nl.hannahsten.texifyidea.psi.*;
import nl.hannahsten.texifyidea.index.stub.BibtexIdStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class BibtexIdImpl extends BibtexIdImplMixin implements BibtexId {

  public BibtexIdImpl(@NotNull BibtexIdStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public BibtexIdImpl(@NotNull ASTNode node) {
    super(node);
  }

  public BibtexIdImpl(BibtexIdStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
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

}
