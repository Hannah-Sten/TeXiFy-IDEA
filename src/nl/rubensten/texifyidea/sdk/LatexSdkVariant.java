package nl.rubensten.texifyidea.sdk;

/**
 * @author Sten Wessel
 */
public enum LatexSdkVariant {
    MIKTEX_2_9("2.9");

    private final String version;

    LatexSdkVariant(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
