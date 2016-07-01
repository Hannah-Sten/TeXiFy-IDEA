// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class LatexVisitor extends PsiElementVisitor {

  public void visitCommand(@NotNull LatexCommand o) {
    visitPsiElement(o);
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

  public void visitGroup(@NotNull LatexGroup o) {
    visitPsiElement(o);
  }

  public void visitInlineMath(@NotNull LatexInlineMath o) {
    visitPsiElement(o);
  }

  public void visitMathEnvironment(@NotNull LatexMathEnvironment o) {
    visitPsiElement(o);
  }

  public void visitNoMathContent(@NotNull LatexNoMathContent o) {
    visitPsiElement(o);
  }

  public void visitOpenGroup(@NotNull LatexOpenGroup o) {
    visitPsiElement(o);
  }

  public void visitOptionalParam(@NotNull LatexOptionalParam o) {
    visitPsiElement(o);
  }

  public void visitParameter(@NotNull LatexParameter o) {
    visitPsiElement(o);
  }

  public void visitRequiredParam(@NotNull LatexRequiredParam o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
