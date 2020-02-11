// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.StubBasedPsiElement;
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub;
import com.intellij.psi.LiteralTextEscaper;

public interface LatexEnvironment extends PsiLanguageInjectionHost, StubBasedPsiElement<LatexEnvironmentStub> {

  @NotNull
  LatexBeginCommand getBeginCommand();

  @Nullable
  LatexEndCommand getEndCommand();

  @Nullable
  LatexEnvironmentContent getEnvironmentContent();

  String getEnvironmentName();

  String getLabel();

  boolean isValidHost();

  PsiLanguageInjectionHost updateText(@NotNull String text);

  @NotNull
  LiteralTextEscaper<LatexEnvironment> createLiteralTextEscaper();

}
