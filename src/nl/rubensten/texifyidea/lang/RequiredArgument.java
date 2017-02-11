package nl.rubensten.texifyidea.lang;

/**
 * @author Sten Wessel
 */
public class RequiredArgument extends Argument {
    RequiredArgument(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "{" + getName() + "}";
    }
}
