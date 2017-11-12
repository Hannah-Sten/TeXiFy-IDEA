package nl.rubensten.texifyidea.structure.latex;

import nl.rubensten.texifyidea.util.TexifyUtil;

/**
 * @author Ruben Schellekens
 */
public class SectionNumbering {

    /**
     * <ul>
     * <li>0: Part</li>
     * <li>1: Chapter</li>
     * <li>2: Section</li>
     * <li>3: Subsection</li>
     * <li>4: Subsubsection</li>
     * <li>5: Paragraph</li>
     * <li>6: Subparagraph</li>
     * </ul>
     */
    private final int counters[] = new int[7];
    private final DocumentClass documentClass;

    public SectionNumbering(DocumentClass documentClass) {
        this.documentClass = documentClass;
    }

    public void increase(int level) {
        counters[level]++;

        // Parts don't reset other counters.
        if (level == 0) {
            return;
        }

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
        // Parts
        if (level == 0) {
            return TexifyUtil.toRoman(Math.max(0, counters[0]));
        }

        StringBuilder sb = new StringBuilder();
        String delimiter = "";

        for (int i = documentClass.startIndex; i <= level; i++) {
            sb.append(delimiter);
            sb.append(getCounter(i));
            delimiter = ".";
        }

        return sb.toString();
    }

    public enum DocumentClass {

        BOOK(1),
        ARTICLE(2);

        private final int startIndex;

        DocumentClass(int startIndex) {
            this.startIndex = startIndex;
        }

        public static DocumentClass getClassByName(String name) {
            if (BOOK.getName().equals(name)) {
                return BOOK;
            }

            return ARTICLE;
        }

        public String getName() {
            return name().toLowerCase();
        }
    }
}
