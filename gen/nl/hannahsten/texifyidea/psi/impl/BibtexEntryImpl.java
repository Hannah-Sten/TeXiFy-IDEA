// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStub;
import nl.hannahsten.texifyidea.psi.*;
import nl.hannahsten.texifyidea.util.BibtexKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class BibtexEntryImpl extends StubBasedPsiElementBase<BibtexEntryStub> implements BibtexEntry {

  public BibtexEntryImpl(ASTNode node) {
    super(node);
  }

  public BibtexEntryImpl(BibtexEntryStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull BibtexVisitor visitor) {
    visitor.visitEntry(this);
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
  @NotNull
  public BibtexEndtry getEndtry() {
    return findNotNullChildByClass(BibtexEndtry.class);
  }

  @Override
  @Nullable
  public BibtexEntryContent getEntryContent() {
    return findChildByClass(BibtexEntryContent.class);
  }

  @Override
  @Nullable
  public BibtexId getId() {
    return findChildByClass(BibtexId.class);
  }

  @Override
  @Nullable
  public BibtexPreamble getPreamble() {
    return findChildByClass(BibtexPreamble.class);
  }

  @Override
  @NotNull
  public BibtexType getType() {
    return findNotNullChildByClass(BibtexType.class);
  }

  @Override
  public String getTitle() {
    BibtexEntryStub stub = getStub();
    if (stub != null) return stub.getTitle();
    return getTagContent("title");
  }

  @Override
  public List<String> getAuthors() {
    BibtexEntryStub stub = getStub();
    if (stub != null) return stub.getAuthors();
    String authorList = getTagContent("author");
    return Arrays.asList(authorList.split(" and "));
  }

  @Override
  public String getYear() {
    BibtexEntryStub stub = getStub();
    if (stub != null) return stub.getYear();

    return getTagContent("year");
  }

  @Override
  public String getIdentifier() {
    BibtexEntryStub stub = getStub();
    if (stub != null) return stub.getIdentifier();
    String identifier = BibtexKt.identifier(this);
    if (identifier == null) return "";
    return identifier;
  }

  public String getAbstract() {
    return getTagContent("abstract");
  }

  private String getTagContent(String tagName) {
    BibtexEntryContent entryContent = getEntryContent();
    if (entryContent == null) return "";

      for (BibtexTag bibtexTag : entryContent.getTagList()) {
        BibtexContent content = bibtexTag.getContent();
        if (tagName.equalsIgnoreCase(bibtexTag.getKey().getText())) {
          String text = BibtexKt.evaluate(content);

          // sanitise double braced strings
          if (text.charAt(0) == '{' && text.charAt(text.length()-1) == '}') {
            return text.substring(1, text.length() - 1);
          }

          return text;
        }
      }

      return "";
  }
  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    return this;
  }

  @Override
  public String getName() {
    return getIdentifier();
  }
}
