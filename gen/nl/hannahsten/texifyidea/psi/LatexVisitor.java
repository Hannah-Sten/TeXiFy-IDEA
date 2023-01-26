// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiLanguageInjectionHost;

public class LatexVisitor extends PsiElementVisitor {

  public void visitBeginCommand(@NotNull LatexBeginCommand o) {
    visitCommandWithParams(o);
  }

  public void visitCommands(@NotNull LatexCommands o) {
    visitPsiNameIdentifierOwner(o);
    // visitCommandWithParams(o);
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

  public void visitKeyValContent(@NotNull LatexKeyValContent o) {
    visitPsiElement(o);
  }

  public void visitKeyValKey(@NotNull LatexKeyValKey o) {
    visitPsiElement(o);
  }

  public void visitKeyValPair(@NotNull LatexKeyValPair o) {
    visitKeyValuePair(o);
  }

  public void visitKeyValValue(@NotNull LatexKeyValValue o) {
    visitPsiElement(o);
  }

  public void visitMagicComment(@NotNull LatexMagicComment o) {
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
    visitPsiElement(o);
  }

  public void visitOptionalParam(@NotNull LatexOptionalParam o) {
    visitPsiElement(o);
  }

  public void visitOptionalParamContent(@NotNull LatexOptionalParamContent o) {
    visitPsiElement(o);
  }

  public void visitParameter(@NotNull LatexParameter o) {
    visitPsiLanguageInjectionHost(o);
  }

  public void visitParameterGroup(@NotNull LatexParameterGroup o) {
    visitPsiElement(o);
  }

  public void visitParameterGroupText(@NotNull LatexParameterGroupText o) {
    visitPsiElement(o);
  }

  public void visitParameterText(@NotNull LatexParameterText o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitPictureParam(@NotNull LatexPictureParam o) {
    visitPsiElement(o);
  }

  public void visitPictureParamContent(@NotNull LatexPictureParamContent o) {
    visitPsiElement(o);
  }

  public void visitPseudocodeBlock(@NotNull LatexPseudocodeBlock o) {
    visitPsiElement(o);
  }

  public void visitPseudocodeBlockContent(@NotNull LatexPseudocodeBlockContent o) {
    visitPsiElement(o);
  }

  public void visitRawText(@NotNull LatexRawText o) {
    visitPsiElement(o);
  }

  public void visitRequiredParam(@NotNull LatexRequiredParam o) {
    visitPsiElement(o);
  }

  public void visitRequiredParamContent(@NotNull LatexRequiredParamContent o) {
    visitPsiElement(o);
  }

  public void visitStrictKeyValPair(@NotNull LatexStrictKeyValPair o) {
    visitKeyValuePair(o);
  }

  public void visitPsiLanguageInjectionHost(@NotNull PsiLanguageInjectionHost o) {
    visitElement(o);
  }

  public void visitPsiNameIdentifierOwner(@NotNull PsiNameIdentifierOwner o) {
    visitElement(o);
  }

  public void visitCommandWithParams(@NotNull LatexCommandWithParams o) {
    visitPsiElement(o);
  }

  public void visitKeyValuePair(@NotNull LatexKeyValuePair o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
