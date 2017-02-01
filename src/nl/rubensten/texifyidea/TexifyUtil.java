package nl.rubensten.texifyidea;

import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexParameter;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sten Wessel
 */
public class TexifyUtil {
    public static List<LatexRequiredParam> getRequiredParameters(LatexCommands command) {
        return command.getParameterList().stream()
                .filter(p -> p.getRequiredParam() != null)
                .map(LatexParameter::getRequiredParam)
                .collect(Collectors.toList());
    }
}
