// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface LatexKeyvalValue extends PsiNameIdentifierOwner {

  @NotNull
  List<LatexKeyvalContent> getKeyvalContentList();

  PsiElement getNameIdentifier();

  String getName();

  PsiElement setName(String name);

}
