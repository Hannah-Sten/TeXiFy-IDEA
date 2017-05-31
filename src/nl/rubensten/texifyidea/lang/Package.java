package nl.rubensten.texifyidea.lang;

/**
 * @author Ruben Schellekens
 */
public class Package {

    protected static final String[] EMPTY_ARRAY = new String[0];

    // Predefined packages.
    public static final Package DEFAULT = new Package("");
    public static final Package FONTENC = new Package("fontenc");
    public static final Package GRAPHICX = new Package("graphicx");

    // Members
    private final String name;
    private final String[] parameters;

    public Package(String name, String... parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Package(String name) {
        this(name, EMPTY_ARRAY);
    }

    /**
     * Creates a new package object with the same name and with the given parameters.
     */
    public Package with(String... parameters) {
        return new Package(name, parameters);
    }

    /**
     * Checks if this package is the default package ('no package').
     *
     * @return {@code true} when is the default package, {@code false} if it is any other package.
     */
    public boolean isDefault() {
        return equals(DEFAULT);
    }

    public String getName() {
        return name;
    }

    public String[] getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Package)) {
            return false;
        }

        Package aPackage = (Package)o;
        return name != null ? name.equals(aPackage.name) : aPackage.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Package{" + name + "}";
    }
}
