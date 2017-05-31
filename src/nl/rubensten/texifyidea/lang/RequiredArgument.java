package nl.rubensten.texifyidea.lang;

/**
 * @author Sten Wessel
 */
public class RequiredArgument extends Argument {

    RequiredArgument(String name) {
        this(name, Type.NORMAL);
    }

    RequiredArgument(String name, Type type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return "{" + getName() + "}";
    }
}
