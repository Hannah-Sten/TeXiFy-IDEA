// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.BibtexTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStub;
import nl.hannahsten.texifyidea.psi.*;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class BibtexEntryImpl extends StubBasedPsiElementBase<BibtexEntryStub> implements BibtexEntry {

  public BibtexEntryImpl(@NotNull BibtexEntryStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public BibtexEntryImpl(@NotNull ASTNode node) {
    super(node);
  }

  public BibtexEntryImpl(BibtexEntryStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitEntry(this);
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
  @NotNull
  public BibtexEndtry getEndtry() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, BibtexEndtry.class));
  }

  @Override
  @Nullable
  public BibtexEntryContent getEntryContent() {
    return PsiTreeUtil.getChildOfType(this, BibtexEntryContent.class);
  }

  @Override
  @Nullable
  public BibtexId getId() {
    return PsiTreeUtil.getChildOfType(this, BibtexId.class);
  }

  @Override
  @Nullable
  public BibtexPreamble getPreamble() {
    return PsiTreeUtil.getChildOfType(this, BibtexPreamble.class);
  }

  @Override
  @NotNull
  public BibtexType getType() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, BibtexType.class));
  }

  @Override
  public PsiReference[] getReferences() {
    return BibtexPsiImplUtil.getReferences(this);
  }

  @Override
  public String getTitle() {
    return BibtexPsiImplUtil.getTitle(this);
  }

  @Override
  public List<String> getAuthors() {
    return BibtexPsiImplUtil.getAuthors(this);
  }

  @Override
  public String getYear() {
    return BibtexPsiImplUtil.getYear(this);
  }

  @Override
  public String getIdentifier() {
    return BibtexPsiImplUtil.getIdentifier(this);
  }

  @Override
  public PsiElement getNameIdentifier() {
    return BibtexPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public String getAbstract() {
    return BibtexPsiImplUtil.getAbstract(this);
  }

  @Override
  public String getTagContent(String tagName) {
    return BibtexPsiImplUtil.getTagContent(this, tagName);
  }

  @Override
  public String getName() {
    return BibtexPsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(@NotNull @NonNls String name) {
    return BibtexPsiImplUtil.setName(this, name);
  }

  @Override
  public String toString() {
    return BibtexPsiImplUtil.toString(this);
  }

  @Override
  public boolean equals(Object other) {
    return BibtexPsiImplUtil.equals(this, other);
  }

  @Override
  public int hashCode() {
    return BibtexPsiImplUtil.hashCode(this);
  }

}
