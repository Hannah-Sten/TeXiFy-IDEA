// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import com.intellij.psi.PsiReference;

public interface LatexCommands extends PsiNamedElement, StubBasedPsiElement<LatexCommandsStub> {

  @NotNull
  List<LatexParameter> getParameterList();

  @NotNull
  PsiElement getCommandToken();

  @NotNull
  PsiReference[] getReferences();

  List<String> getOptionalParameters();

  List<String> getRequiredParameters();

  boolean hasLabel();

}
