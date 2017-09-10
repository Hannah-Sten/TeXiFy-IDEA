package nl.rubensten.texifyidea.lang;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ruben Schellekens
 */
public interface Environment extends Dependend {

    /**
     * Looks up a default environment by the given name.
     *
     * @param environmentName
     *         The name of the environment object to get.
     * @return The {@link DefaultEnvironment} with the given name, or {@code null} when it couldn't
     * be found.
     */
    @Nullable
    static Environment lookup(String environmentName) {
        return DefaultEnvironment.get(environmentName);
    }

    /**
     * Get what type of myContext this enviroment has inside.
     */
    Context getContext();

    /**
     * Get the contents that must be placed into the environment just after it has been
     * inserted using the auto complete.
     */
    String getInitialContents();

    /**
     * Get the name of the environment.
     */
    String getEnvironmentName();

    /**
     * Get all the environment myArguments.
     */
    Argument[] getArguments();

    /**
     * @author Ruben Schellekens
     */
    enum Context {
        NORMAL,
        MATH,
        COMMENT
    }
}
