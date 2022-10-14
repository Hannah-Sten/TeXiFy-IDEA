// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public class BibtexVisitor extends PsiElementVisitor {

  public void visitBracedString(@NotNull BibtexBracedString o) {
    visitPsiElement(o);
  }

  public void visitBracedVerbatim(@NotNull BibtexBracedVerbatim o) {
    visitPsiElement(o);
  }

  public void visitComment(@NotNull BibtexComment o) {
    visitPsiElement(o);
  }

  public void visitContent(@NotNull BibtexContent o) {
    visitPsiElement(o);
  }

  public void visitDefinedString(@NotNull BibtexDefinedString o) {
    visitPsiElement(o);
  }

  public void visitEndtry(@NotNull BibtexEndtry o) {
    visitPsiElement(o);
  }

  public void visitEntry(@NotNull BibtexEntry o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitEntryContent(@NotNull BibtexEntryContent o) {
    visitPsiElement(o);
  }

  public void visitId(@NotNull BibtexId o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitKey(@NotNull BibtexKey o) {
    visitPsiElement(o);
  }

  public void visitNormalText(@NotNull BibtexNormalText o) {
    visitPsiElement(o);
  }

  public void visitPreamble(@NotNull BibtexPreamble o) {
    visitPsiElement(o);
  }

  public void visitQuotedString(@NotNull BibtexQuotedString o) {
    visitPsiElement(o);
  }

  public void visitQuotedVerbatim(@NotNull BibtexQuotedVerbatim o) {
    visitPsiElement(o);
  }

  public void visitRawText(@NotNull BibtexRawText o) {
    visitPsiElement(o);
  }

  public void visitString(@NotNull BibtexString o) {
    visitPsiElement(o);
  }

  public void visitTag(@NotNull BibtexTag o) {
    visitPsiElement(o);
  }

  public void visitType(@NotNull BibtexType o) {
    visitPsiElement(o);
  }

  public void visitPsiNameIdentifierOwner(@NotNull PsiNameIdentifierOwner o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
