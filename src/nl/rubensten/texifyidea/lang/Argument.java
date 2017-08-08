package nl.rubensten.texifyidea.lang;

/**
 * @author Sten Wessel
 */
public abstract class Argument {

    public static final Argument[] EMPTY_ARRAY = new Argument[0];

    private String name;
    private Type type;

    protected Argument(String name) {
        this(name, Type.NORMAL);
    }

    protected Argument(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public abstract String toString();

    public enum Type {
        NORMAL,
        FILE,
        TEXT
    }
}
