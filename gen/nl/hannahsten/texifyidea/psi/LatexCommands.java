// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import java.util.LinkedHashMap;

public interface LatexCommands extends PsiNameIdentifierOwner, LatexCommandWithParams, StubBasedPsiElement<LatexCommandsStub> {

  @NotNull
  List<LatexParameter> getParameterList();

  @NotNull
  PsiElement getCommandToken();

  LinkedHashMap<LatexKeyValKey, LatexKeyValValue> getOptionalParameterMap();

  List<String> getRequiredParameters();

  int getTextOffset();

  String getName();

  PsiElement setName(String name);

}
