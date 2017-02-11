package nl.rubensten.texifyidea.lang;

/**
 * @author Sten Wessel
 */
public abstract class Argument {

    private String name;

    protected Argument(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
