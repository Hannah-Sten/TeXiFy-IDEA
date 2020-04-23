// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiLanguageInjectionHost;

public class LatexVisitor extends PsiElementVisitor {

  public void visitBeginCommand(@NotNull LatexBeginCommand o) {
    visitPsiElement(o);
  }

  public void visitCommands(@NotNull LatexCommands o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitComment(@NotNull LatexComment o) {
    visitPsiElement(o);
  }

  public void visitContent(@NotNull LatexContent o) {
    visitPsiElement(o);
  }

  public void visitDisplayMath(@NotNull LatexDisplayMath o) {
    visitPsiElement(o);
  }

  public void visitEndCommand(@NotNull LatexEndCommand o) {
    visitPsiElement(o);
  }

  public void visitEnvironment(@NotNull LatexEnvironment o) {
    visitPsiLanguageInjectionHost(o);
  }

  public void visitEnvironmentContent(@NotNull LatexEnvironmentContent o) {
    visitPsiElement(o);
  }

  public void visitGroup(@NotNull LatexGroup o) {
    visitPsiElement(o);
  }

  public void visitInlineMath(@NotNull LatexInlineMath o) {
    visitPsiElement(o);
  }

  public void visitMathContent(@NotNull LatexMathContent o) {
    visitPsiElement(o);
  }

  public void visitMathEnvironment(@NotNull LatexMathEnvironment o) {
    visitPsiElement(o);
  }

  public void visitNoMathContent(@NotNull LatexNoMathContent o) {
    visitPsiElement(o);
  }

  public void visitNormalText(@NotNull LatexNormalText o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitOptionalParam(@NotNull LatexOptionalParam o) {
    visitPsiElement(o);
  }

  public void visitOptionalParamContent(@NotNull LatexOptionalParamContent o) {
    visitPsiElement(o);
  }

  public void visitParameter(@NotNull LatexParameter o) {
    visitPsiElement(o);
  }

  public void visitRawText(@NotNull LatexRawText o) {
    visitPsiElement(o);
  }

  public void visitRequiredParam(@NotNull LatexRequiredParam o) {
    visitPsiElement(o);
  }

  public void visitPsiLanguageInjectionHost(@NotNull PsiLanguageInjectionHost o) {
    visitElement(o);
  }

  public void visitPsiNameIdentifierOwner(@NotNull PsiNameIdentifierOwner o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
