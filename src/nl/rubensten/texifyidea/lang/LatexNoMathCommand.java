package nl.rubensten.texifyidea.lang;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Sten Wessel
 */
public enum LatexNoMathCommand {

    A_RING("aa", "å"),
    CAPITAL_A_RING("AA", "Å"),
    AE("ae", "æ"),
    CAPITAL_AE("AE", "Æ"),
    APPENDIX("appendix"),
    AUTHOR("author", required("names")),
    BEGIN("begin", required("environment")),
    END("end", required("environment")),
    ENSUREMATH("ensuremath", required("text")),
    BASELINESKIP("baselineskip"),
    BASELINESTRETCH("baselinestretch "),
    BFSERIES("bfseries"),
    BIBITEM("bibitem", optional("label"), required("citekey")),
    BIBLIOGRAPHYSTYLE("bibliographystyle", required("style")),
    BIBLIOGRAPHY("bibliography", new RequiredFileArgument("bibliographyfile", "bib")),
    BIGSKIP("bigskip"),
    BOLDMATH("boldmath"),
    CAPTION("caption", optional("shorttext"), required("text")),
    CHAPTER("chapter", optional("shorttitle"), required("title")),
    CHAPTER_STAR("chapter*", required("title")),
    CITE("cite", optional("extratext"), required("keys")),
    CLEARDOUBLEPAGE("cleardoublepage"),
    CLEARPAGE("clearpage"),
    COLUMNSEP("columnsep "),
    COLUMNWIDTH("columnwidth"),
    CONTENTSLINE("contentsline", required("type"), required("text"), required("page")),
    CONTENTSNAME("contentsname", required("name")),
    DATE("date", required("text")),
    DOCUMENTCLASS("documentclass", optional("options"), required("class")),
    DOTFILL("dotfill"),
    EM("em"),
    EMPH("emph", required("text")),
    ENLARGETHISPAGE("enlargethispage", required("size")),
    ENLARGETHISPAGE_STAR("enlargethispage*", required("size")),
    EVENSIDEMARGIN("evensidemargin"),
    FAMILY("family"),
    FBOX("fbox", required("text")),
    FIGURENAME("figurename", required("name")),
    FLQ("flq", "‹"),
    FLQQ("flqq", "«"),
    FLUSHBOTTOM("flushbottom"),
    FLUSHLEFT("flushleft"),
    FLUSHRIGHT("flushright"),
    FONTENCODING("fontencoding", required("enc")),
    FONTFAMILY("fontfamily", required("family")),
    FONTSERIES("fontseries", required("series")),
    FONTSHAPE("fontshape", required("shape")),
    FONTSIZE("fontsize", required("size"), required("skip")),
    FOOTNOTESIZE("footnotesize"),
    FOOTNOTETEXT("footnotetext", optional("number"), required("text")),
    FOOTNOTE("footnote", optional("number"), required("text")),
    FRAMEBOX("framebox", optional("width"), optional("pos"), optional("text")),
    FRAME("frame", required("text")),
    FRQ("frq", "›"),
    FRQQ("frqq", "»"),
    GLOSSARYENTRY("glossaryentry", required("text"), required("pagenum")),
    GLOSSARY("glossary", required("text")),
    GLQ("glq", ","),
    GLQQ("glqq", "„"),
    GRQ("grq", "‘"),
    GRQQ("grqq", "“"),
    HFILL("hfill"),
    HRULE("hrule"),
    HRULEFILL("hrulefill"),
    HSPACE("hspace", required("length")),
    HSPACE_STAR("hspace*", required("length")),
    HSS("hss"),
    HUGE("huge"),
    CAPITAL_HUGE("Huge"),
    HYPHENATION("hyphenation", required("words")),
    I("i", "i (dotless)"),
    INCLUDE("include", new RequiredFileArgument("sourcefile", "tex")),
    INPUT("input", new RequiredFileArgument("sourcefile", "tex")),
    INCLUDEONLY("includeonly", new RequiredFileArgument("sourcefile", "tex")),
    INDEXNAME("indexname", required("name")),
    INDEXSPACE("indexspace"),
    INDEX("intex", required("entry")),
    ITEM("item", optional("label")),
    ITSHAPE("itshape"),
    LABEL("label", required("key")),
    LARGE("large"),
    CAPITAL_LARGE("Large"),
    SCREAMING_LARGE("LARGE"),
    LATEX("LaTeX", "LaTeX"),
    LATEXE("LaTeXe", "LaTeX2ε"),
    LDOTS("ldots", "…"),
    LEFTEQN("lefteqn"),
    LINEBREAK("linebreak", optional("number")),
    LINETHICKNESS("linethickness", required("dimension")),
    LINEWIDTH("linewidth"),
    LISTFIGURENAME("listfigurename", required("name")),
    LISTFILES("listfiles"),
    LISTOFFIGURES("listoffigures"),
    LISTOFTABLES("listoftables"),
    LISTTABLENAME("listtablename", required("name")),
    MAKEATLETTER("makeatletter"),
    MAKEATOTHER("makeatother"),
    MAKEGLOSSARY("makeglossary"),
    MAKEINDEX("makeindex"),
    MAKELABEL("makelabel"),
    MAKELABELS("makelabels", required("number")),
    MAKETITLE("maketitle"),
    MBOX("mbox", required("text")),
    MEDSKIP("medskip"),
    MULTICOLUMN("multicolumn", required("cols"), required("pos"), required("text")),
    NEWLABEL("newlabel"),
    NEWLENGTH("newlength", required("length")),
    NEWLINE("newline"),
    NEWPAGE("newpage"),
    NEWTHEOREM("newtheorem", required("envname"), optional("numberedlike"), required("caption"), optional("within")),
    NEWTHEOREM_STAR("newtheorem*", required("envname"), required("caption")),
    NOCITE("nocite", required("keys")),
    NOFILES("nofiles"),
    NOLINEBREAK("nolinebreak", optional("number")),
    NONUMBER("nonumber"),
    NOPAGEBREAK("nopagebreak", optional("number")),
    NORMALCOLOR("normalcolor"),
    NORMALFONT("normalfont"),
    NORMALSIZE("normalsize"),
    OE("oe", "œ"),
    CAPITAL_OE("OE", "Œ"),
    ODDSIDEMARGIN("oddsidemargin"),
    ONECOLUMN("onecolumn"),
    PAGEBREAK("pagebreak", optional("number")),
    PAGENAME("pagename"),
    PAGENUMBERING("pagenumbering", required("numstyle")),
    PAGEREF("pageref", required("label")),
    PAGESTYLE("pagestyle", required("style")),
    PAGETOTAL("pagetotal"),
    PAPERWIDTH("paperwidth"),
    PAPERHEIGHT("paperheight"),
    PARAGRAPH("paragraph", optional("shorttitle"), required("title")),
    PARAGRAPH_STAR("paragraph*", required("title")),
    PARAGRAPHMARK("paragraphmark"),
    PARBOX("parbox", optional("pos"), required("width"), required("text")),
    PARINDENT("parindent"),
    PARSKIP("parskip"),
    PART("part", optional("shorttitle"), required("title")),
    PART_STAR("part*", required("title")),
    PARTNAME("partname", required("name")),
    PDFINFO("pdfinfo", required("info")),
    POUNDS("pounds", "£"),
    PRINTINDEX("printindex"),
    R("r", "˚ (accent)"),
    REF("ref", required("label")),
    REFNAME("refname", required("name")),
    RIGHTHYPHENMIN("righthyphenmin"),
    RIGHTMARGIN("rightmargin"),
    RIGHTMARK("rightmark"),
    RMFAMILY("rmfamily"),
    ROMAN("roman", required("counter")),
    CAPITAL_ROBAN("Roman", required("counter")),
    RULE("rule", optional("line"), required("width"), required("thickness")),
    SAMEPAGE("samepage"),
    SBOX("sbox", required("cmd"), required("length")),
    SCRIPTSIZE("scriptsize"),
    SCSHAPE("scshape"),
    SECTION("section", optional("shorttitle"), required("title")),
    SECTION_STAR("section", required("title")),
    SELECTFONT("selectfont"),
    SETCOUNTER("setcounter", required("countername"), required("value")),
    SETLENGTH("setlength", required("cmd"), required("length")),
    SFFAMILY("sffamily"),
    SHORTSTACK("shortstack", optional("pos"), required("text")),
    SLSHAPE("slshape"),
    SMALL("small"),
    SMALLSKIP("smallskip"),
    SMASH("smash"),
    SPACE("space"),
    STEPCOUNTER("stepcounter", required("counter")),
    STOP("stop"),
    STRETCH("stretch", required("factor")),
    SUBITEM("subitem"),
    SUBPARAGRAPH("subparagraph", optional("shorttitle"), required("title")),
    SUBPARAGRAPH_STAR("subparagraph*", required("title")),
    SUBPARAGRAPHMARK("subparagraphmark", required("code")),
    SUBSECTION("subsection", optional("shorttitle"), required("title")),
    SUBSECTION_STAR("subsection*", required("title")),
    SUBSECTIONMARK("subsectionmark", required("code")),
    SUBSUBITEM("subsubitem"),
    SUBSUBSECTION("subsubsection", optional("shorttitle"), required("title")),
    SUBSUBSECTION_STAR("subsubsection*", required("title")),
    SUBSUBSECTIONMARK("subsubsectionmark", required("code")),
    SUPPRESSFLOATS("suppressfloats", optional("placement")),
    SYMBOL("symbol", required("n")),
    TABCOLSEP("tabcolsep"),
    TABLENAME("tablename", required("name")),
    TABLEOFCONTENTS("tableofcontents"),
    TEXTASCIICIRCUM("textasciicircum", "^"),
    TEXTASCIITILDE("textasciitilde", "~"),
    TEXTASTERISKCENTERED("textasciicentered", "⁎"),
    TEXTBACKSLASH("textbackslash", "\\"),
    TEXTBAR("textbar", "|"),
    TEXTBF("textbf", required("text")),
    TEXTBRACELEFT("textbraceleft", "{"),
    TEXTBRACERIGHT("textbraceright", "}"),
    TEXTBULLET("textbullet", "•"),
    TEXTCIRCLED("textcircled", required("text")),
    TEXTCOPYRIGHT("textcopyright", "©"),
    TEXTDAGGER("textdagger", "†"),
    TEXTDAGGERDBL("textdaggerdbl", "‡"),
    TEXTDOLLAR("textdollar", "$"),
    TEXTELLIPSIS("textellipsis", "…"),
    TEXTEMDASH("textemdash", "—"),
    TEXTENDASH("textendash", "–"),
    TEXTEXCLAMDOWN("textexclamdown", "¡"),
    TEXTGREATER("textgreater", ">"),
    TEXTHEIGHT("textheight"),
    TEXTIT("textit", required("text")),
    TEXTLESS("textless"),
    TEXTMD("textmd", required("text")),
    TEXTNORMAL("textnormal"),
    TEXTPARAGRAPH("textparagraph"),
    TEXTPERIODCENTERED("textperiodcentered", "·"),
    TEXTQUESTIONDOWN("textquestiondown", "¿"),
    TEXTQUOTEDBLLEFT("textquotedblleft", "“"),
    TEXTQUOTEDBLRIGHT("textquotedblright", "”"),
    TEXTQUOTELEFT("textquoteleft", "‘"),
    TEXTQUOTERIGHT("textquoteright", "’"),
    TEXTREGISTERED("textregistered", "®"),
    TEXTRM("textrm", required("text")),
    TEXTSC("textsc", required("textsc")),
    TEXTSECTION("textsection", "§"),
    TEXTSF("textsf", required("text")),
    TEXTSL("textsl", required("text")),
    TEXTSTERLING("textsterling", "£"),
    TEXTSUBSCRIPT("textsubscript", required("text")),
    TEXTSUPERSCRIPT("textsuperscript", required("text")),
    TEXTTRADEMARK("texttrademark", "™"),
    TEXTTT("texttt", required("text")),
    TEXTUNDERSCORE("textunderscore", "_"),
    TEXTUP("textup", required("text")),
    TEXTVISIBLESPACE("textvisiblespace", "␣"),
    TEXTWIDTH("textwidth"),
    THANKS("thanks", required("to")),
    THICKLINES("thicklines"),
    THINLINES("thinlines"),
    THISPAGESTYLE("thispagestyle", required("style")),
    TIME("time"),
    TINY("tiny"),
    TITLE("title", required("text")),
    TODAY("today"),
    TOPMARGIN("topmargin"),
    TTFAMILY("ttfamily"),
    TWOCOLUMN("twocolumn", optional("text")),
    UNBOLDMATH("unboldmath"),
    UNDERLINE("underline", required("text")),
    UNITLENGTH("unitlength"),
    UPSHAPE("upshape"),
    USEPACKAGE("usepackage", optional("options"), required("package")),
    VDOTS("vdots", "⋮"),
    VLINE("vline"),
    VSPACE("vspace", required("length")),
    VSPACE_STAR("vspace*", required("length")),
    WIDTH("width"),

    /*
     *  New definitions
     */
    NEWCOMMAND("newcommand",
            required("cmd"), optional("args"),
            optional("default"), required("def")),
    NEWCOMMAND_STAR("newcommand*", required("cmd"),
            optional("args"), optional("default"),
            required("def")),
    PROVIDECOMMAND("providecommand", required("cmd"),
            optional("args"), optional("default"),
            required("def")),
    PROVIDECOMMAND_STAR("providecommand*", required("cmd"),
            optional("args"), optional("default"),
            required("def")),
    RENEWCOMMAND("renewcommand", required("cmd"),
            optional("args"), optional("default"),
            required("def")),
    RENEWCOMMAND_STAR("renewcommand*", required("cmd"),
            optional("args"), optional("default"),
            required("def")),
    NEWENVIRONMENT("newenvironment", required("name"),
            optional("args"), optional("default"),
            required("begdef"), required("enddef")),
    RENEWENVIRONMENT("renewenvironment", required("name"),
            optional("args"), optional("default"),
            required("begdef"), required("enddef")),;

    private static final Map<String, LatexNoMathCommand> lookup = new HashMap<>();

    static {
        for (LatexNoMathCommand command : LatexNoMathCommand.values()) {
            lookup.put(command.getCommand(), command);
        }
    }

    private String command;
    private Argument[] arguments;
    private String display;

    LatexNoMathCommand(String command, String display, Argument... arguments) {
        this(command, arguments);
        this.display = display;
    }

    LatexNoMathCommand(String command, Argument... arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public static Optional<LatexNoMathCommand> get(String command) {
        return Optional.ofNullable(lookup.get(command));
    }

    private static RequiredArgument required(String name) {
        return new RequiredArgument(name);
    }

    private static OptionalArgument optional(String name) {
        return new OptionalArgument(name);
    }

    public String getCommand() {
        return command;
    }

    public String getCommandDisplay() {
        return "\\" + command;
    }

    public Argument[] getArguments() {
        return arguments;
    }

    public <T extends Argument> List<T> getArgumentsOf(Class<T> clazz) {
        List<T> requiredArguments = new ArrayList<>();

        for (Argument argument : arguments) {
            if (clazz.isAssignableFrom(argument.getClass())) {
                requiredArguments.add((T)argument);
            }
        }

        return requiredArguments;
    }

    public String getArgumentsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Argument arg : arguments) {
            sb.append(arg.toString());
        }

        return sb.toString();
    }

    public String getDisplay() {
        return display;
    }

    /**
     * Checks whether {@code {}} must be automatically inserted in the auto complete.
     *
     * @return {@code true} to insert automatically, {@code false} not to insert.
     */
    public boolean autoInsertRequired() {
        return Stream.of(arguments).filter(arg -> arg instanceof RequiredArgument).count() >= 1;
    }
}
