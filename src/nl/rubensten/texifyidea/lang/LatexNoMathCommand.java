package nl.rubensten.texifyidea.lang;

import org.jetbrains.annotations.NotNull;

import nl.rubensten.texifyidea.lang.Argument.Type;

import java.util.*;
import java.util.stream.Stream;

import static nl.rubensten.texifyidea.lang.Package.DEFAULT;
import static nl.rubensten.texifyidea.lang.Package.FONTENC;
import static nl.rubensten.texifyidea.lang.Package.GRAPHICX;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public enum LatexNoMathCommand {

    ADDTOCOUNTER("addtocounter", required("countername"), required("value")),
    A_RING("aa", "å"),
    CAPITAL_A_RING("AA", "Å"),
    AE("ae", "æ"),
    CAPITAL_AE("AE", "Æ"),
    APPENDIX("appendix"),
    AUTHOR("author", requiredText("name")),
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
    CAPTION("caption", optional("shorttext"), requiredText("text")),
    CHAPTER("chapter", optional("shorttitle"), requiredText("title")),
    CHAPTER_STAR("chapter*", requiredText("title")),
    CITE("cite", optional("extratext"), required("keys")),
    CLEARDOUBLEPAGE("cleardoublepage"),
    CLEARPAGE("clearpage"),
    COLUMNSEP("columnsep "),
    COLUMNWIDTH("columnwidth"),
    CONTENTSLINE("contentsline", required("type"), requiredText("text"), required("page")),
    CONTENTSNAME("contentsname", required("name")),
    DATE("date", requiredText("text")),
    DEF("def"),
    DOCUMENTCLASS("documentclass", optional("options"), required("class")),
    DOTFILL("dotfill"),
    EM("em"),
    EMPH("emph", requiredText("text")),
    ENLARGETHISPAGE("enlargethispage", required("size")),
    ENLARGETHISPAGE_STAR("enlargethispage*", required("size")),
    EVENSIDEMARGIN("evensidemargin"),
    FAMILY("family"),
    FBOX("fbox", requiredText("text")),
    FIGURENAME("figurename", requiredText("name")),
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
    FOOTNOTETEXT("footnotetext", optional("number"), requiredText("text")),
    FOOTNOTE("footnote", optional("number"), requiredText("text")),
    FRAMEBOX("framebox", optional("width"), optional("pos"), optionalText("text")),
    FRAME("frame", requiredText("text")),
    FRQ("frq", "›"),
    FRQQ("frqq", "»"),
    GLOSSARYENTRY("glossaryentry", requiredText("text"), required("pagenum")),
    GLOSSARY("glossary", requiredText("text")),
    GLQ("glq", ","),
    GLQQ("glqq", "„"),
    GRQ("grq", "‘"),
    GRQQ("grqq", "“"),
    GUILLEMOTLEFT("guillemotleft", FONTENC.with("T1"), ""),
    GUILLEMOTRIGHT("guillemotright", FONTENC.with("T1"), ""),
    HFILL("hfill"),
    HRULE("hrule"),
    HRULEFILL("hrulefill"),
    HSPACE("hspace", required("length")),
    HSPACE_STAR("hspace*", required("length")),
    HSS("hss"),
    HUGE("huge"),
    CAPITAL_HUGE("Huge"),
    HYPHENATION("hyphenation", requiredText("words")),
    I("i", "i (dotless)"),
    INCLUDE("include", new RequiredFileArgument("sourcefile", "tex")),
    INPUT("input", new RequiredFileArgument("sourcefile", "tex")),
    INCLUDEGRAPHICS("includegraphics", GRAPHICX, optional("key-val-list"), new RequiredFileArgument("imagefile", "pdf", "png", "jpg", "eps")),
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
    LET("let"),
    LINEBREAK("linebreak", optional("number")),
    LINETHICKNESS("linethickness", required("dimension")),
    LINEWIDTH("linewidth"),
    LISTFIGURENAME("listfigurename", requiredText("name")),
    LISTFILES("listfiles"),
    LISTOFFIGURES("listoffigures"),
    LISTOFTABLES("listoftables"),
    LISTTABLENAME("listtablename", requiredText("name")),
    MAKEATLETTER("makeatletter"),
    MAKEATOTHER("makeatother"),
    MAKEGLOSSARY("makeglossary"),
    MAKEINDEX("makeindex"),
    MAKELABEL("makelabel"),
    MAKELABELS("makelabels", required("number")),
    MAKETITLE("maketitle"),
    MBOX("mbox", required("text")),
    MEDSKIP("medskip"),
    MULTICOLUMN("multicolumn", required("cols"), required("pos"), requiredText("text")),
    NEWLABEL("newlabel"),
    NEWLENGTH("newlength", required("length")),
    NEWLINE("newline"),
    NEWPAGE("newpage"),
    NEWTHEOREM("newtheorem", required("envname"), optional("numberedlike"), requiredText("caption"), optional("within")),
    NEWTHEOREM_STAR("newtheorem*", required("envname"), requiredText("caption")),
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
    PARAGRAPH("paragraph", optionalText("shorttitle"), requiredText("title")),
    PARAGRAPH_STAR("paragraph*", requiredText("title")),
    PARAGRAPHMARK("paragraphmark"),
    PARBOX("parbox", optional("pos"), required("width"), requiredText("text")),
    PARINDENT("parindent"),
    PARSKIP("parskip"),
    PART("part", optionalText("shorttitle"), requiredText("title")),
    PART_STAR("part*", requiredText("title")),
    PARTNAME("partname", requiredText("name")),
    PDFINFO("pdfinfo", requiredText("info")),
    POUNDS("pounds", "£"),
    PRINTINDEX("printindex"),
    R("r", "˚ (accent)"),
    REF("ref", required("label")),
    REFNAME("refname", requiredText("name")),
    RIGHTHYPHENMIN("righthyphenmin"),
    RIGHTMARGIN("rightmargin"),
    RIGHTMARK("rightmark"),
    RMFAMILY("rmfamily"),
    ROMAN("roman", required("counter")),
    ROTATEBOX("rotatebox", GRAPHICX, optional("key-val-list"), required("degrees"), requiredText("text")),
    CAPITAL_ROBAN("Roman", required("counter")),
    RULE("rule", optional("line"), required("width"), required("thickness")),
    SAMEPAGE("samepage"),
    SBOX("sbox", required("cmd"), required("length")),
    SCRIPTSIZE("scriptsize"),
    SCSHAPE("scshape"),
    SECTION("section", optionalText("shorttitle"), requiredText("title")),
    SECTION_STAR("section", requiredText("title")),
    SELECTFONT("selectfont"),
    SETCOUNTER("setcounter", required("countername"), required("value")),
    SETLENGTH("setlength", required("cmd"), required("length")),
    SFFAMILY("sffamily"),
    SHORTSTACK("shortstack", optional("pos"), requiredText("text")),
    SLSHAPE("slshape"),
    SMALL("small"),
    SMALLSKIP("smallskip"),
    SMASH("smash"),
    SPACE("space"),
    STEPCOUNTER("stepcounter", required("counter")),
    STOP("stop"),
    STRETCH("stretch", required("factor")),
    SUBITEM("subitem"),
    SUBPARAGRAPH("subparagraph", optionalText("shorttitle"), requiredText("title")),
    SUBPARAGRAPH_STAR("subparagraph*", requiredText("title")),
    SUBPARAGRAPHMARK("subparagraphmark", required("code")),
    SUBSECTION("subsection", optionalText("shorttitle"), requiredText("title")),
    SUBSECTION_STAR("subsection*", requiredText("title")),
    SUBSECTIONMARK("subsectionmark", required("code")),
    SUBSUBITEM("subsubitem"),
    SUBSUBSECTION("subsubsection", optionalText("shorttitle"), requiredText("title")),
    SUBSUBSECTION_STAR("subsubsection*", requiredText("title")),
    SUBSUBSECTIONMARK("subsubsectionmark", required("code")),
    SUPPRESSFLOATS("suppressfloats", optional("placement")),
    SYMBOL("symbol", required("n")),
    TABCOLSEP("tabcolsep"),
    TABLENAME("tablename", requiredText("name")),
    TABLEOFCONTENTS("tableofcontents"),
    TEXTASCIICIRCUM("textasciicircum", "^"),
    TEXTASCIITILDE("textasciitilde", "~"),
    TEXTASTERISKCENTERED("textasciicentered", "⁎"),
    TEXTBACKSLASH("textbackslash", "\\"),
    TEXTBAR("textbar", "|"),
    TEXTBF("textbf", requiredText("text")),
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
    TEXTIT("textit", requiredText("text")),
    TEXTLESS("textless"),
    TEXTMD("textmd", requiredText("text")),
    TEXTNORMAL("textnormal"),
    TEXTPARAGRAPH("textparagraph"),
    TEXTPERIODCENTERED("textperiodcentered", "·"),
    TEXTQUESTIONDOWN("textquestiondown", "¿"),
    TEXTQUOTEDBLLEFT("textquotedblleft", "“"),
    TEXTQUOTEDBLRIGHT("textquotedblright", "”"),
    TEXTQUOTELEFT("textquoteleft", "‘"),
    TEXTQUOTERIGHT("textquoteright", "’"),
    TEXTREGISTERED("textregistered", "®"),
    TEXTRM("textrm", requiredText("text")),
    TEXTSC("textsc", requiredText("textsc")),
    TEXTSECTION("textsection", "§"),
    TEXTSF("textsf", requiredText("text")),
    TEXTSL("textsl", requiredText("text")),
    TEXTSTERLING("textsterling", "£"),
    TEXTSUBSCRIPT("textsubscript", requiredText("text")),
    TEXTSUPERSCRIPT("textsuperscript", requiredText("text")),
    TEXTTRADEMARK("texttrademark", "™"),
    TEXTTT("texttt", requiredText("text")),
    TEXTUNDERSCORE("textunderscore", "_"),
    TEXTUP("textup", requiredText("text")),
    TEXTVISIBLESPACE("textvisiblespace", "␣"),
    TEXTWIDTH("textwidth"),
    THANKS("thanks", requiredText("to")),
    THICKLINES("thicklines"),
    THINLINES("thinlines"),
    THISPAGESTYLE("thispagestyle", required("style")),
    TIME("time"),
    TINY("tiny"),
    TITLE("title", requiredText("text")),
    TODAY("today"),
    TOPMARGIN("topmargin"),
    TTFAMILY("ttfamily"),
    TWOCOLUMN("twocolumn", optionalText("text")),
    UNBOLDMATH("unboldmath"),
    UNDERLINE("underline", requiredText("text")),
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
            optional("default"), requiredText("def")),
    NEWCOMMAND_STAR("newcommand*", required("cmd"),
            optional("args"), optional("default"),
            requiredText("def")),
    PROVIDECOMMAND("providecommand", required("cmd"),
            optional("args"), optional("default"),
            requiredText("def")),
    PROVIDECOMMAND_STAR("providecommand*", required("cmd"),
            optional("args"), optional("default"),
            requiredText("def")),
    RENEWCOMMAND("renewcommand", required("cmd"),
            optional("args"), optional("default"),
            requiredText("def")),
    RENEWCOMMAND_STAR("renewcommand*", required("cmd"),
            optional("args"), optional("default"),
            requiredText("def")),
    NEWENVIRONMENT("newenvironment", required("name"),
            optional("args"), optional("default"),
            requiredText("begdef"), requiredText("enddef")),
    RENEWENVIRONMENT("renewenvironment", required("name"),
            optional("args"), optional("default"),
            requiredText("begdef"), requiredText("enddef")),;

    private static final Map<String, LatexNoMathCommand> lookup = new HashMap<>();
    static {
        for (LatexNoMathCommand command : LatexNoMathCommand.values()) {
            lookup.put(command.getCommand(), command);
        }
    }

    private final String command;
    private final Package thePackage;
    private final Argument[] arguments;
    private final String display;

    LatexNoMathCommand(String command, Package thePackage, String display, Argument... arguments) {
        this.command = command;
        this.thePackage = thePackage;
        this.arguments = arguments;
        this.display = display;
    }

    LatexNoMathCommand(String command, Package thePackage, Argument... arguments) {
        this(command, thePackage, null, arguments);
    }

    LatexNoMathCommand(String command, String display, Argument... arguments) {
        this(command, DEFAULT, display, arguments);
    }

    LatexNoMathCommand(String command, Argument... arguments) {
        this(command, DEFAULT, arguments);
    }

    public static Optional<LatexNoMathCommand> get(String command) {
        return Optional.ofNullable(lookup.get(command));
    }

    private static RequiredArgument required(String name) {
        return new RequiredArgument(name);
    }

    private static RequiredArgument requiredText(String name) {
        return new RequiredArgument(name, Type.TEXT);
    }

    private static OptionalArgument optional(String name) {
        return new OptionalArgument(name);
    }

    private static OptionalArgument optionalText(String name) {
        return new OptionalArgument(name, Type.TEXT);
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

    @NotNull
    public Package getPackage() {
        return thePackage;
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
