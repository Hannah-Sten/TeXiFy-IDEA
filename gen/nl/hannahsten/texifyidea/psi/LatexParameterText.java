// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;

public interface LatexParameterText extends PsiNameIdentifierOwner {

  @NotNull
  List<LatexCommands> getCommandsList();

  PsiReference[] getReferences();

  PsiReference getReference();

  PsiElement getNameIdentifier();

  String getName();

  PsiElement setName(String name);

  void delete();

}
