package nl.rubensten.texifyidea.structure;

/**
 * @author Ruben Schellekens
 */
public class SectionNumbering {

    public static final int SECTION_LEVEL = 0;
    public static final int SUBSECTION_LEVEL = 1;
    public static final int SUBSUBSECTION_LEVEL = 2;
    public static final int PARAGRAPH_LEVEL = 3;
    public static final int SUBPARAGRAPH_LEVEL = 4;

    private int counters[] = new int[5];

    public void increase(int level) {
        counters[level]++;

        for (int i = level + 1; i < counters.length; i++) {
            counters[i] = 0;
        }
    }

    public int getCounter(int level) {
        return counters[level];
    }

    public void setCounter(int level, int amount) {
        counters[level] = amount;
    }

    public void addCounter(int level, int amount) {
        counters[level] += amount;
    }

    public String getTitle(int level) {
        StringBuilder sb = new StringBuilder();
        String delimiter = "";

        for (int i = 0; i <= level; i++) {
            sb.append(delimiter);
            sb.append(getCounter(i));
            delimiter = ".";
        }

        return sb.toString();
    }
}
