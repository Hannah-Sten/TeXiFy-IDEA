package nl.rubensten.texifyidea.index;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.ArrayUtil;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Ruben Schellekens
 */
public class LatexIncludesIndex extends StringStubIndexExtension<LatexCommands> {

    public static final StubIndexKey<String, LatexCommands> KEY =
            StubIndexKey.createIndexKey("nl.rubensten.texifyidea.includes");

    private static final Pattern PRECEDING_SLASH = Pattern.compile("^\\\\");

    @NotNull
    public static Collection<LatexCommands> getIncludes(@NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        return getIncludes(project, scope);
    }

    @NotNull
    public static Collection<LatexCommands> getIncludes(@NotNull Project project,
                                                        @NotNull GlobalSearchScope scope) {
        Collection<LatexCommands> commands = new ArrayList<>();

        for (String key : getKeys(project)) {
            commands.addAll(getIncludesByName(key, project, scope));
        }

        return commands;
    }

    @NotNull
    public static Collection<LatexCommands> getIndexedIncludesByName(@NotNull String name,
                                                                     @NotNull Project project,
                                                                     @NotNull GlobalSearchScope scope) {
        Collection<LatexCommands> commands = new ArrayList<>();

        for (String key : getKeys(project)) {
            Collection<LatexCommands> cmds = getIncludesByName(key, project, scope);

            for (LatexCommands cmd : cmds) {
                String cmdToken = PRECEDING_SLASH.matcher(cmd.getCommandToken().getText()).replaceFirst("");
                String matchTo = PRECEDING_SLASH.matcher(name).replaceFirst("");

                if (!cmdToken.equals(matchTo)) {
                    continue;
                }

                commands.add(cmd);
            }
        }

        return commands;
    }

    @NotNull
    public static Collection<LatexCommands> getIncludesByName(@NotNull String name,
                                                              @NotNull Project project,
                                                              @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(KEY, name, project, scope, LatexCommands.class);
    }

    @NotNull
    public static String[] getKeys(@NotNull Project project) {
        return ArrayUtil.toStringArray(StubIndex.getInstance().getAllKeys(LatexCommandsIndex.KEY, project));
    }

    @NotNull
    @Override
    public StubIndexKey<String, LatexCommands> getKey() {
        return KEY;
    }
}
