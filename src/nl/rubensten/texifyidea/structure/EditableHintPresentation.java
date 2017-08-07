package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens
 */
public interface EditableHintPresentation extends ItemPresentation {

    void setHint(@NotNull String hint);

}
