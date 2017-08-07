package nl.rubensten.texifyidea.lang;

/**
 * @author Sten Wessel
 */
public class OptionalArgument extends Argument {

    OptionalArgument(String name) {
        this(name, Type.NORMAL);
    }

    OptionalArgument(String name, Type type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return "[" + getName() + "]";
    }
}
