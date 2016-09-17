package nl.rubensten.texifyidea.run;

/**
 * @author Sten Wessel
 */
public enum LatexCompiler {
    PDFLATEX("pdfLaTeX");

    private String displayName;


    LatexCompiler(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
