package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LArgument.Companion.required
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet

object PredefinedCmdGeneric : PredefinedCommandSet() {
    private val textArg = required("text", LatexContexts.Text)
    private val labelArg = required("label", LatexContexts.LabelReference)

    val genericCommands = buildCommands {
        val titleArg = required("title", LatexContexts.Text)

        symbol("LaTeX", "LaTeX")
        symbol("LaTeXe", "LaTeX2ε")
        "\\".cmd("margin".optional) { "Linebreak" }
        symbol("AA", "Å")
        symbol("AE", "Æ")
        +"Huge"
        +"LARGE"
        +"Large"

        // spacing
        "quad".cmd { "space equal to the current font size (= 18 mu) " }
        ",".cmd { "1/6 of \\quad (= 3 mu) " }
        ":".cmd { "2/9 of \\quad (= 4 mu) " }
        ";".cmd { "5/18 of \\quad (= 5 mu) " }
        "!".cmd { "negative space, equal to -3 mu" }
        " ".cmd { "equivalent of space in normal text " }

        // special characters

        "Roman".cmd("counter".required) { "Format a counter value as uppercase Roman numerals." }

        "addtocounter".cmd("countername".required, "value".required) { "Add a value to a counter." }
        "addcontentsline".cmd("file".required, "type".required, textArg) { "Add an entry to a contents file (for example toc, lof, lot)." }
        +"appendix"
        "author".cmd("name".required) { "Set the document author." }
        +"baselineskip"
        +"baselinestretch"
        +"bf"
        +"bfseries"
        "bibitem".cmd("label".optional, "citekey".required(LatexContexts.BibKey))

        +"bigskip"
        +"boldmath"
        "caption".cmd("shorttext".optional, textArg) { "Set a caption for a float." }
        "captionof".cmd("float type".required, "list entry".optional, "heading".required) { "Set a caption outside a float environment." }
        "chapter".cmd("shorttitle".optional, titleArg) { "Create a chapter heading." }
        "chapter*".cmd(titleArg) { "Create an unnumbered chapter heading." }

        +"cleardoublepage"
        +"clearpage"
        +"columnsep "
        +"columnwidth"
        "contentsline".cmd("type".required, textArg, "page".required) { "Write a line directly to a contents file." }
        "contentsname".cmd("name".required) { "Set the title used for the table of contents." }

        "date".cmd(textArg) { "Set the document date." }

        +"dotfill"
        +"em"
        "emph".cmd(textArg) { "Emphasize text." }

        "enlargethispage".cmd("size".required) { "Increase the available space on the current page." }
        "enlargethispage*".cmd("size".required) { "Increase the available space on the current page, including stretchable space." }
        +"evensidemargin"

        +"family"
        "fbox".cmd(textArg) { "Draw a frame around text." }
        "figurename".cmd("name".required) { "Set the label used for figures." }

        +"flushbottom"
        +"flushleft"
        +"flushright"
        "fontencoding".cmd("enc".required) { "Select a font encoding." }
        "fontfamily".cmd("family".required) { "Select a font family." }
        "fontseries".cmd("series".required) { "Select a font series (for example bold)." }
        "fontshape".cmd("shape".required) { "Select a font shape (for example italic)." }
        "fontsize".cmd("size".required, "skip".required) { "Set the font size and line spacing." }
        "footnote".cmd("number".optional, textArg) { "Insert a footnote." }
        +"footnotemark"
        +"footnotesize"
        "footnotetext".cmd("number".optional, textArg) { "Insert footnote text without a marker in the main text." }
        "frame".cmd(textArg) { "Frame content." }
        "framebox".cmd("width".optional, "pos".optional, "text".optional) { "Create a framed box, optionally with width and alignment." }

        "glossary".cmd(textArg) { "Add a glossary entry." }
        "glossaryentry".cmd(textArg, "pagenum".required) { "Define a glossary entry with page information." }

        +"hfill"
        +"hrule"
        +"hrulefill"
        "hspace".cmd("length".required) { "Insert horizontal space." }
        "hspace*".cmd("length".required) { "Insert horizontal space that is kept at line breaks." }
        +"hss"
        +"huge"
        "hyphenation".cmd("words".required) { "Declare hyphenation patterns for words." }

        "ifthenelse".cmd("test".required, "then clause".required, "else clause".required) { "Evaluate a condition and choose between two branches." }

        "indexname".cmd("name".required) { "Set the title used for the index." }
        +"indexspace"
        "intex".cmd("entry".required) { "Add an index entry." }
        +"it"
        "item".cmd("label".optional(LatexContexts.Text)) { "Start a list item." }
        +"itshape"

        +"large"
        +"lefteqn"
        +"lfseries"
        "linebreak".cmd("number".optional) { "Request a line break." }
        "linethickness".cmd("dimension".required) { "Set line thickness for picture-mode drawing commands." }
        +"linewidth"
        "listfigurename".cmd("name".required) { "Set the title used for the list of figures." }
        +"listfiles"
        +"listoffigures"
        +"listoftables"
        "listtablename".cmd("name".required) { "Set the title used for the list of tables." }
        "lowercase".cmd(textArg) { "Convert text to lowercase." }

        +"makeglossary"
        +"makeindex"
        +"makelabel"
        "makelabels".cmd("number".required) { "Configure label creation." }
        +"maketitle"
        "marg".cmd("arg".required) { "Typeset an argument placeholder in documentation." }
        "mbox".cmd(textArg) { "Keep content together in horizontal mode." }
        +"mdseries"
        +"medskip"
        "meta".cmd("arg".required) { "Typeset a meta-variable placeholder in documentation." }
        "multicolumn".cmd("cols".required, "pos".required, textArg) { "Span multiple columns in a table." }
        +"newlabel"
        "newlength".cmd("length".required) { "Define a new length register." }
        +"newline"
        +"newpage"
        "nocite".cmd("keys".required) { "Add bibliography entries without citing them in the text." }
        +"nofiles"
        "nolinebreak".cmd("number".optional) { "Discourage a line break at this point." }
        +"nonumber"
        "nopagebreak".cmd("number".optional) { "Discourage a page break at this point." }
        +"normalfont"
        +"normalsize"
        "oarg".cmd("arg".required) { "Typeset an optional argument placeholder in documentation." }
        +"oddsidemargin"

        "oldstylenums".cmd("number".required) { "Typeset numbers using old-style numerals." }
        +"onecolumn"
        "onlyifstandalone".cmd("code".required) { "Execute code only when compiling as a standalone document." }
        "pagebreak".cmd("number".optional) { "Request a page break." }
        +"pagename"
        "pagenumbering".cmd("numstyle".required) { "Set page numbering style." }

        "pagestyle".cmd("style".required) { "Set the page style for subsequent pages." }
        +"pagetotal"
        +"paperheight"
        +"paperwidth"
        "paragraph".cmd("shorttitle".optional, titleArg) { "Create a paragraph heading." }
        "paragraph*".cmd(titleArg) { "Create an unnumbered paragraph heading." }
        +"paragraphmark"
        "parbox".cmd("pos".optional, "width".required, textArg) { "Create a paragraph box of fixed width." }
        "parg".cmd("arg".required) { "Typeset a delimited argument placeholder in documentation." }
        +"parindent"
        +"parskip"
        "part".cmd("shorttitle".optional, titleArg) { "Create a part heading." }
        "part*".cmd(titleArg) { "Create an unnumbered part heading." }
        "partname".cmd("name".required) { "Set the label used for parts." }
        "pdfinfo".cmd("info".required) { "Set PDF metadata entries." }

        +"printindex"

        +"righthyphenmin"
        +"rightmargin"
        +"rightmark"
        +"rm"
        +"rmfamily"
        "roman".cmd("counter".required) { "Format a counter value as lowercase Roman numerals." }

        "rule".cmd("line".optional, "width".required, "thickness".required) { "Draw a rule with the given width and thickness." }
        +"samepage"
        "sbox".cmd("cmd".required, "length".required) { "Store content in a box register." }
        +"sc"
        +"scriptsize"
        +"scshape"
        "section".cmd("shorttitle".optional, titleArg) { "Create a section heading." }
        "section*".cmd(titleArg) { "Create an unnumbered section heading." }
        +"selectfont"
        "setcounter".cmd("countername".required, "value".required) { "Set a counter to a specific value." }
        "setlength".cmd("cmd".required, "length".required) { "Set a length register." }
        +"sf"
        +"sffamily"
        "shortstack".cmd("pos".optional, textArg) { "Stack short lines vertically." }
        +"sl"
        +"slshape"
        +"small"
        +"smallskip"
        +"smash"
        +"space"
        "stepcounter".cmd("counter".required) { "Increment a counter by one." }
        +"stop"
        "stretch".cmd("factor".required) { "Create stretchable horizontal space with a factor." }
        +"subitem"
        "subparagraph".cmd("shorttitle".optional, titleArg) { "Create a subparagraph heading." }
        "subparagraph*".cmd(titleArg) { "Create an unnumbered subparagraph heading." }
        "subparagraphmark".cmd("code".required) { "Define how subparagraph marks appear in running headers." }
        "subsection".cmd("shorttitle".optional, titleArg) { "Create a subsection heading." }
        "subsection*".cmd(titleArg) { "Create an unnumbered subsection heading." }
        "subsectionmark".cmd("code".required) { "Define how subsection marks appear in running headers." }
        +"subsubitem"
        "subsubsection".cmd("shorttitle".optional, titleArg) { "Create a subsubsection heading." }
        "subsubsection*".cmd(titleArg) { "Create an unnumbered subsubsection heading." }
        "subsubsectionmark".cmd("code".required) { "Define how subsubsection marks appear in running headers." }
        "suppressfloats".cmd("placement".optional(LatexContexts.Position)) { "Prevent floats from being placed in the specified area." }
        "symbol".cmd("n".required) { "Typeset the character with the given symbol number." }
        +"tabcolsep"
        "tablename".cmd("name".required) { "Set the label used for tables." }
        +"tableofcontents"

        "textbf".cmd(textArg) { "Typeset text in boldface." }

        "textcircled".cmd("a".required) { "Draw a circle around text." }

        +"textheight"
        "textit".cmd(textArg) { "Typeset text in italic." }

        "textlf".cmd(textArg) { "Typeset text in light font weight." }
        "textmd".cmd(textArg) { "Typeset text in medium font weight." }
        +"textnormal"
        +"textparagraph"

        "textrm".cmd(textArg) { "Typeset text in roman family." }
        "textsc".cmd("textsc".required) { "Typeset text in small caps." }

        "textsf".cmd(textArg) { "Typeset text in sans-serif family." }
        "textsl".cmd(textArg) { "Typeset text in slanted shape." }

        "textsubscript".cmd(textArg) { "Typeset text as subscript." }
        "textsuperscript".cmd(textArg) { "Typeset text as superscript." }

        "texttt".cmd(textArg) { "Typeset text in monospaced family." }

        "textup".cmd(textArg) { "Typeset text in upright shape." }

        +"textwidth"
        "thanks".cmd("to".required) { "Add a footnote-style acknowledgment, typically in the title block." }
        +"thicklines"
        +"thinlines"
        "thispagestyle".cmd("style".required) { "Set the page style for the current page only." }
        +"time"
        +"tiny"
        "title".cmd(textArg) { "Set the document title." }
        +"today"
        +"topmargin"
        +"tt"
        +"ttfamily"
        "twocolumn".cmd("text".optional) { "Switch to two-column layout." }
        +"unboldmath"
        "underline".cmd(textArg) { "Underline text." }
        +"unitlength"
        "uppercase".cmd(textArg) { "Convert text to uppercase." }
        +"upshape"
        "usepgfplotslibrary".cmd("libraries".required) { "Load PGFPlots libraries." }
        "usetikzlibrary".cmd("libraries".required) { "Load TikZ libraries." }
        +"vline"
        "vspace".cmd("length".required) { "Insert vertical space." }
        "vspace*".cmd("length".required) { "Insert vertical space that is kept at page breaks." }
        +"width"

        packageOf("biblatex")
        +"printbibliography"

        packageOf("amsmath")
        "eqref".cmd(labelArg) { "Reference an equation number in parentheses." }

        packageOf("csquotes")
        +"enquote"
        +"enquote*"

        packageOf("fontenc")
        symbol("guillemotleft", "«")
        symbol("guillemotright", "»")

        packageOf("mathtools")
        symbol("lparen", "(")
        symbol("rparen", ")")

        packageOf("glossaries")
        +"makenoidxglossaries"
        +"printnoidxglossaries"

        packageOf("amsthm")
        +"qedhere"

        packageOf("ulem")
        "sout".cmd("strikethroughtext".required) { "Strike out text." }

        packageOf("ntheorem")
        "thref".cmd(labelArg) { "Reference a theorem-like environment label." }

        packageOf("fontspec")
        "addfontfeature".cmd("font features".required) { "Add font features to the current font selection." }
        "addfontfeatures".cmd("font features".required) { "Add multiple font features to the current font selection." }
        "defaultfontfeatures".cmd("font names".optional, "font features".required) { "Set default font features." }
        "fontspec".cmd("font".required, "font features".optional) { "Select a font and optional features (fontspec)." }
        "setmainfont".cmd("font".required, "font features".optional) { "Set the main text font." }
        "setmonofont".cmd("font".required, "font features".optional) { "Set the monospaced font." }
        "setsansfont".cmd("font".required, "font features".optional) { "Set the sans-serif font." }
    }

    val citation = textCommands {
        val before = "before".optional
        val after = "after".optional
        val keys = "keys".required(LatexContexts.BibReference)

        "cite".cmd("extratext".optional, keys) { "CITE" }
        "bibliographystyle".cmd("style".required(LatexContexts.BibStyle)) { "BIBLIOGRAPHYSTYLE" }

        packageOf("natbib")
        arrayOf(
            "Citealp", "Citealp*", "Citealt", "Citealt*", "Citep", "Citep*", "Citet", "Citet*",
            "citealp", "citealp*", "citealt", "citealt*", "citep", "citep*", "citet", "citet*"
        ).forEach { it.cmd(before, after, keys) }

        arrayOf(
            "Citeauthor", "Citeauthor*", "citeauthor", "citeauthor*", "citenum",
            "citetitle", "citetitle*", "citeyear", "citeyear*", "citeyearpar"
        ).forEach { it.cmd(keys) }

        "citetext".cmd(textArg)

        packageOf("biblatex")
        val prenote = "prenote".optional
        val postnote = "postnote".optional
        val key = "keys".required(LatexContexts.BibReference)
        val volume = "volume".required
        val page = "page".optional
        val citeCommands = listOf(
            "Autocite", "Autocite*", "Autocites", "Avolcite", "Avolcites", "Cite",
            "Citeauthor", "Citeauthor*", "Cites", "Ftvolcite", "Fvolcite", "Fvolcites",
            "Notecite", "Parencite", "Parencites", "Pnotecite", "Pvolcite", "Pvolcites",
            "Smartcite", "Smartcites", "Svolcite", "Svolcites", "Textcite", "Textcites",
            "Tvolcite", "Tvolcites", "Volcite", "Volcites",
            "autocite", "autocite*", "autocites", "avolcite", "avolcites",
            "cite*", "citeauthor", "citeauthor*", "citedate", "citedate*", "cites",
            "citetitle", "citetitle*", "citeurl", "citeyear", "citeyear*", "fnotecite",
            "footcite", "footcites", "footcitetext", "footcitetexts", "footfullcite",
            "fullcite", "notecite", "parencite", "parencite*", "parencites",
            "pnotecite", "smartcite", "smartcites", "supercite", "supercites",
            "textcite", "textcites"
        )
        citeCommands.forEach { it.cmd(prenote, postnote, key) }

        val citeWithVolume = listOf(
            "Avolcite", "Avolcites", "Ftvolcite", "Fvolcite", "Fvolcites",
            "Pvolcite", "Pvolcites", "Svolcite", "Svolcites", "Tvolcite", "Tvolcites", "Volcite", "Volcites",
            "avolcite", "avolcites",
            "ftvolcite", "ftvolcites", "fvolcite", "fvolcites", "pvolcite", "pvolcites",
            "svolcite", "svolcites", "tvolcite", "tvolcites", "volcite", "volcites"
        )
        citeWithVolume.forEach { it.cmd(prenote, volume, page, key) }

        val citeWithTextArg = listOf("brackettext", "parenttext")
        citeWithTextArg.forEach { it.cmd(textArg) }
        "nocite".cmd(key)

        underPackage(LatexLib.CITATION_STYLE_LANGUAGE) {
            val options = "options".optional
            "cslsetup".cmd(options)

            listOf("cite", "parencite", "citep", "textcite", "citet", "footcite", "cites", "citeyearpar", "fullcite").forEach {
                it.cmd(options, keys)
            }
            listOf("citeauthor", "citeyear").forEach {
                it.cmd("key".required(LatexContexts.BibReference))
            }
            "nocite".cmd(keys)
            "printbibliography".cmd(options)
            "newrefsection".cmd(options)
            "endrefsection".cmd()
            "defbibheading".cmd("name".required, "title".optional, "code".required)
        }
    }

    val reference = buildCommands {

        "label".cmd("key".required(LatexContexts.LabelDefinition)) {
            "Define a label for referencing"
        }

        val label1 = required("label1", LatexContexts.LabelReference)
        val label2 = required("label2", LatexContexts.LabelReference)
        "ref".cmd(labelArg) { "Reference to a label" }
        "pageref".cmd(labelArg) { "Page reference to a label" }

        +"refname"

        underPackage("nameref") {
            "nameref".cmd(labelArg) { "Reference to a label" }
            "Nameref".cmd(labelArg) { "Reference to a label with page number" }
        }

        underPackage("cleveref") {
            "cref".cmd(labelArg) { "Reference a label with an automatically chosen type name." }
            "Cpageref".cmd(labelArg) { "Reference the page of a label with a capitalized prefix." }
            "Cpagerefrange".cmd(label1, label2) { "Reference a page range between two labels with a capitalized prefix." }
            "Cref".cmd(labelArg) { "Reference a label with an automatically chosen, capitalized type name." }
            "cpageref".cmd(labelArg) { "Reference the page of a label with an automatically chosen prefix." }
            "cpagerefrange".cmd(label1, label2) { "Reference a page range between two labels with an automatically chosen prefix." }
            "crefrange".cmd(label1, label2) { "Reference a range between two labels with an automatically chosen type name." }
            "labelcpageref".cmd(labelArg) { "Print the formatted label text used by cpageref for a label." }
            "labelcref".cmd(labelArg) { "Print the formatted label text used by cref for a label." }
            "lcnamecref".cmd(labelArg) { "Print the lowercase singular type name for a label." }
            "lcnamecrefs".cmd(labelArg) { "Print the lowercase plural type name for a label." }
            "nameCref".cmd(labelArg) { "Print the capitalized singular type name for a label." }
            "nameCrefs".cmd(labelArg) { "Print the capitalized plural type name for a label." }
            "namecref".cmd(labelArg) { "Print the singular type name for a label." }
            "namecrefs".cmd(labelArg) { "Print the plural type name for a label." }
        }

        underPackage("zref-clever") {
            "zcref".cmd("options".optional, labelArg) { "Reference a zref label with an automatically chosen type name." }
            "zcref*".cmd("options".optional, labelArg) { "Reference a zref label with an automatically chosen type name (starred variant)." }
            "zcpageref".cmd("options".optional, labelArg) { "Reference the page of a zref label with zref-clever formatting." }
            "zcpageref*".cmd("options".optional, labelArg) { "Reference the page of a zref label with zref-clever formatting (starred variant)." }
        }

        underPackage("zref") {
            val zrefProps = "properties".optional(LatexContexts.Literal)
            val zrefSetup = "options".required(LatexContexts.Literal)
            "zlabel".cmd("label".required(LatexContexts.LabelDefinition)) { "Define a zref label with extended properties." }
            "zref".cmd(zrefProps, labelArg) { "Reference a zref label with optional property selection." }
            "zpageref".cmd(labelArg) { "Reference the page number of a zref label." }
            "zrefused".cmd(labelArg) { "Mark a zref label as used." }
            "zxrsetup".cmd(zrefSetup) { "Configure zref cross-document reference behavior." }
        }

        underPackage("zref-xr") {
            val prefix = "prefix".optional(LatexContexts.Literal)
            val externalDocument = "external-document".required(LatexContexts.SingleFile)
            val url = "url".optional(LatexContexts.URL)
            "zexternaldocument".cmd(prefix, externalDocument, url) { "Import zref labels from an external document." }
            "zexternaldocument*".cmd(prefix, externalDocument, url) { "Import zref labels from an external document (starred variant)." }
        }

        underPackage("zref-titleref") {
            "ztitleref".cmd(labelArg) { "Reference the title associated with a zref label." }
            "ztitlerefsetup".cmd("options".required(LatexContexts.Literal)) { "Configure title references provided by zref." }
        }

        underPackage("zref-savepos") {
            val zrefDef = "label".required(LatexContexts.LabelDefinition)
            "zsavepos".cmd(zrefDef) { "Save the current position under a zref label." }
            "zsaveposx".cmd(zrefDef) { "Save the current horizontal position under a zref label." }
            "zsaveposy".cmd(zrefDef) { "Save the current vertical position under a zref label." }
            "zposx".cmd(labelArg) { "Get saved horizontal position from a zref label." }
            "zposy".cmd(labelArg) { "Get saved vertical position from a zref label." }
        }

        underPackage("zref-perpage") {
            "zmakeperpage".cmd("reset".optional(LatexContexts.Literal), "counter".required(LatexContexts.Literal)) {
                "Reset a counter on each page (zref-perpage)."
            }
            "zunmakeperpage".cmd("counter".required(LatexContexts.Literal)) {
                "Disable per-page reset for a counter (zref-perpage)."
            }
        }

        underPackage("zref-nextpage") {
            "znextpage".cmd { "Trigger zref next-page tracking for the current location." }
            "znextpagesetup".cmd(
                "first".required(LatexContexts.Literal),
                "middle".required(LatexContexts.Literal),
                "last".required(LatexContexts.Literal)
            ) { "Configure formatting used by zref-nextpage." }
        }

        underPackage("zref-totpages") {
            "ztotpages".cmd { "Print the total number of pages recorded by zref." }
        }

        underPackage("zref-thepage") {
            "zthepage".cmd("abspage".required(LatexContexts.Numeric)) { "Convert absolute page number to formatted page representation." }
        }

        underPackage("zref-lastpage") {
            "ziflastpage".cmd(labelArg, "then".required, "else".required) { "Branch based on whether a label is on the last page." }
            "zref@iflastpage".cmd(labelArg, "then".required, "else".required) { "Internal-style conditional for zref-lastpage." }
        }

        underPackage("hyperref") {
            "Autoref".cmd(labelArg) { "Reference a label with an automatically chosen, capitalized prefix." }
            "autoref".cmd(labelArg) { "Reference a label with an automatically chosen prefix." }
            "fullref".cmd(labelArg) { "Create a full reference (for example name and number) to a label." }
            "hyperref".cmd("options".optional, labelArg) { "Create a hyperlink to a label." }
            "phantomsection".cmd { "Create an invisible hyperlink anchor at the current position." }

            val urlArg = required("url", LatexContexts.URL)
            "href".cmd(urlArg, textArg) { "Insert a hyperlink with custom link text." }
            "url".cmd(urlArg) { "Typeset and link a URL." }
        }

        underPackage("varioref") {
            "Vref".cmd(labelArg) { "VREF_CAPITAL" }
            "vref".cmd(labelArg) { "VREF" }
            "vrefrange".cmd("start".required, "end".required, "text".optional) { "VREFRANGE" }
        }

        underPackage("prettyref") {
            "prettyref".cmd(labelArg)
            underContext(LatexContexts.Preamble) {
                "newrefformat".cmd("prefix".required, "refText".required(setOf(LatexContexts.InsideDefinition, LatexContexts.Text)))
            }
        }
    }

    val algorithm = buildCommands {
        packageOf("algpseudocode")
        applicableIn(LatexContexts.Algorithmicx)
        "ElsIf".cmd("condition".required) { "ELSIF" }
        "EndFor".cmd()
        "EndFunction".cmd()
        "EndIf".cmd()
        "EndLoop".cmd()
        "EndProcedure".cmd()
        "EndWhile".cmd()
        "For".cmd("condition".required) { "FOR" }
        "ForAll".cmd("condition".required) { "FORALL" }
        "Function".cmd("name".required, "params".required) { "FUNCTION" }
        "If".cmd("condition".required) { "IF_ALGPSEUDOCODE" }
        "Loop".cmd()
        "Procedure".cmd("name".required, "params".required) { "PROCEDURE" }
        "Repeat".cmd()
        "Until".cmd("condition".required) { "UNTIL" }
        "While".cmd("condition".required) { "WHILE" }
    }

    val todoCommand = buildCommands {
        packageOf("todonotes")
        "listoftodos".cmd("name".optional) { "LISTOFTODOS" }
        "missingfigure".cmd("note".required) { "MISSINGFIGURE" }
        "todo".cmd("note".required) { "TODO" }
    }

    val glossaries = buildCommands {
        underPackage("glossaries") {
            val options = "options".optional
            val label = "label".required(setOf(LatexContexts.Text, LatexContexts.GlossaryReference))
            val insert = "insert".optional

            underContext(LatexContexts.Preamble) {
                val nameDef = "name".required(setOf(LatexContexts.Text, LatexContexts.GlossaryDefinition))
//              "loadglsentries".cmd("glossariesfile".required(LatexContexts.SingleFile))
                "longnewglossaryentry".cmd(nameDef, "options".required, "description".required)
                "newabbreviation".cmd(options, nameDef, "short".required, "long".required)
                "newacronym".cmd(options, nameDef, "short".required, "long".required)
                "newglossaryentry".cmd(nameDef, "options".required)
            }

            underContext(LatexContexts.Text) {
                listOf(
                    "GLS", "GLSdesc", "GLSfirst", "GLSfirstplural", "GLSname", "GLSplural", "GLSsymbol", "GLStext",
                    "GLSuseri", "GLSuserii", "GLSuseriii", "GLSuseriv", "GLSuserv", "GLSuservi",
                    "Gls", "Glsdesc", "Glsfirst", "Glsfirstplural", "Glsname", "Glspl",
                    "Glsplural", "Glssymbol", "Glstext", "Glsuseri", "Glsuserii", "Glsuseriii", "Glsuseriv",
                    "Glsuserv", "Glsuservi", "gls", "glsdesc", "glsfirst", "glsfirstplural",
                    "glsname", "glspl", "glsplural", "glssymbol", "glstext", "glsuseri",
                    "glsuserii", "glsuseriii", "glsuseriv", "glsuserv", "glsuservi"
                ).forEach { it.cmd(options, label, insert) }
                listOf("Glsdisp", "Glslink", "glsdisp", "glslink").forEach {
                    it.cmd(options, label, "text".optional)
                }
            }
        }

        underPackage("acronym") {
            val linebreakPenalty = "linebreak penalty".optional
            val acronymDef = "acronym".required(setOf(LatexContexts.Text, LatexContexts.GlossaryDefinition))
            val acronymRef = "acronym".required(setOf(LatexContexts.Text, LatexContexts.GlossaryReference))

            underContext(LatexContexts.Preamble) {
                "acro".cmd(acronymDef, "short name".optional, "full name".required)
                "acrodef".cmd(acronymDef, "short name".optional, "full name".required)
                "newacro".cmd(acronymDef, "short name".optional, "full name".required)
            }

            underContext(LatexContexts.Text) {
                arrayOf(
                    "Ac", "Ac*", "Acf", "Acf*", "Acfi", "Acfi*", "Acfip", "Acfip*", "Acfp", "Acfp*",
                    "Acl", "Acl*", "Aclp", "Aclp*", "Aclu", "Aclu*", "Acp", "Acp*", "Iac", "Iac*",
                    "ac", "ac*", "acf", "acf*", "acfi", "acfi*", "acfip", "acfip*", "acfp", "acfp*",
                    "acl", "acl*", "aclp", "aclp*", "aclu", "aclu*", "acp", "acp*", "acs", "acs*",
                    "acsp", "acsp*", "acsu", "acsu*", "iac", "iac*"
                ).forEach { it.cmd(linebreakPenalty, acronymRef) }
            }
        }

        underPackage("acro") {
            val options = "options".optional
            val acroRef = "id".required(setOf(LatexContexts.Text, LatexContexts.GlossaryReference))
            val acroDef = "id".required(setOf(LatexContexts.Text, LatexContexts.GlossaryDefinition))
            val setupOptions = "options".required

            underContext(LatexContexts.Preamble) {
                "DeclareAcronym".cmd(acroDef, setupOptions) { "Declare an acronym entry." }
            }

            "acsetup".cmd(setupOptions) { "Configure acro package options." }

            fun registerAcroCommands(commands: List<String>, description: String) {
                commands.forEach { cmd ->
                    cmd.cmd(options, acroRef) { description }
                    "$cmd*".cmd(options, acroRef) { description }
                }
            }

            underContext(LatexContexts.Text) {
                "printacronyms".cmd(options) { "Print the list of acronyms." }

                // First-use template
                registerAcroCommands(
                    listOf("ac", "acp", "iac", "Ac", "Acp", "Iac"),
                    "Typeset an acronym with the first-use form."
                )

                // Short form
                registerAcroCommands(
                    listOf("acs", "acsp", "iacs", "Acs", "Acsp", "Iacs"),
                    "Typeset the short form of an acronym."
                )

                // Long form
                registerAcroCommands(
                    listOf("acl", "aclp", "iacl", "Acl", "Aclp", "Iacl"),
                    "Typeset the long form of an acronym."
                )

                // Alternative form
                registerAcroCommands(
                    listOf("aca", "acap", "iaca", "Aca", "Acap", "Iaca"),
                    "Typeset the alternative form of an acronym."
                )

                // Full form
                registerAcroCommands(
                    listOf("acf", "acfp", "iacf", "Acf", "Acfp", "Iacf"),
                    "Typeset the full form of an acronym."
                )

                // Show data without usage side-effects
                registerAcroCommands(
                    listOf("acshow"),
                    "Show acronym information without marking it as used."
                )
            }
        }
    }

    val tcolorboxDefinitionCommands = buildCommands {
        packageOf("tcolorbox")
        applicableIn(LatexContexts.Preamble)

        val initOptionsOptional = "init options".optional
        val nameRequired = "name".required
        val numberOptional = "number".optional
        val defaultOptional = "default".optional
        val optionsRequired = "options".required
        val specificationRequired = "specification".required

        "newtcolorbox".cmd(initOptionsOptional, nameRequired, numberOptional, defaultOptional, optionsRequired) { "Define a new tcolorbox environment" }
        "renewtcolorbox".cmd(initOptionsOptional, nameRequired, numberOptional, defaultOptional, optionsRequired) { "Redefine a tcolorbox environment" }

        "DeclareTColorBox".cmd(initOptionsOptional, nameRequired, specificationRequired, optionsRequired) { "Declare a robust tcolorbox" }

        "NewTColorBox".cmd(initOptionsOptional, nameRequired, specificationRequired, optionsRequired) { "New tcolorbox (capitalized variant)" }
        "ReNewTColorBox".cmd(initOptionsOptional, nameRequired, specificationRequired, optionsRequired) { "Renew tcolorbox (capitalized variant)" }
        "ProvideTColorBox".cmd(initOptionsOptional, nameRequired, specificationRequired, optionsRequired) { "Provide a tcolorbox if not defined" }
    }

    val listingsDefinitionCommands = buildCommands {
        packageOf("listings")

        val nameRequired = required("name", LatexContexts.EnvironmentDeclaration)
        val numberOptional = "number".optional
        val defaultArgOptional = "default arg".optional
        val startingCodeRequired = "starting code".required(ctx = LatexContexts.InsideDefinition)
        val endingCodeRequired = "ending code".required(ctx = LatexContexts.InsideDefinition)

        "lstnewenvironment".cmd(nameRequired, numberOptional, defaultArgOptional, startingCodeRequired, endingCodeRequired) { "Define a new listings environment" }

        "lstset".cmd(required("settings"))
    }

    val listings = buildCommands {
        underPackage("luacode") {
            "directlua".cmd("lua code".required(LatexContexts.Verbatim))
            "luaexec".cmd("lua code".required(LatexContexts.Verbatim))
        }
        underPackage("pythontex") {
            listOf("py", "pyb", "pyc", "pys", "pyv").forEach { it.cmd("code".required(LatexContexts.Verbatim)) }
        }
        underPackage("piton") {
            "piton".cmd("code".required(LatexContexts.Verbatim))
        }
        underPackage(LatexLib.LISTINGS) {
            "lstinline".cmd("code".required(LatexContexts.Verbatim))
        }
    }

    val colorDefinitionCommands = buildCommands {
        val colorArg = "color".required(LatexContexts.ColorReference)
        underContext(LatexContexts.Preamble) {
            val typeOpt = "type".optional
            val nameReq = "name".required(LatexContexts.ColorDefinition)
            val modelListReq = "model-list".required(LatexContexts.Literal)
            val specListReq = "spec-list".required(LatexContexts.Literal)

            packageOf("color")
            "definecolor".cmd(typeOpt, nameReq, modelListReq, specListReq) { "Define a color" }
            "DefineNamedColor".cmd("type".required, nameReq, modelListReq, specListReq) { "Define a named color" }

            packageOf("xcolor")
            "providecolor".cmd(typeOpt, nameReq, modelListReq, specListReq) { "Provide a color" }
            "colorlet".cmd(typeOpt, nameReq, "num model".optional, colorArg) { "Define a color based on another" }
            "definecolorset".cmd(typeOpt, modelListReq, "head".required, "tail".required, "set spec".required) { "Define a color set" }
            "providecolorset".cmd(typeOpt, modelListReq, "head".required, "tail".required, "set spec".required) { "Provide a color set" }
            "preparecolor".cmd(typeOpt, nameReq, modelListReq, specListReq) { "Prepare a color" }
            "preparecolorset".cmd(typeOpt, modelListReq, "head".required, "tail".required, "set spec".required) { "Prepare a color set" }
            "definecolors".cmd("id-list".required) { "Define colors" }
            "providecolors".cmd("id-list".required) { "Provide colors" }
            "definecolorseries".cmd(
                "name".required,
                "core model".required,
                "method".required,
                "b-model".optional,
                "b-spec".required,
                "s-model".required,
                "s-spec".required
            ) { "Define a color series" }
        }
    }

    val colorRelatedCommands = buildCommands {
        val colorArg = "color".required(LatexContexts.ColorReference)

        packageOf("xcolor")
        "blendcolors".cmd("mix expr".required) { "BLENDCOLORS" }
        "blendcolors*".cmd("mix expr".required) { "BLENDCOLORS_STAR" }
        "boxframe".cmd("width".required, "height".required, "depth".required) { "BOXFRAME" }
        "color".cmd("model-list".optional, colorArg) { "COLOR2" }
        "colorbox".cmd("model-list".optional, colorArg, "text".required) { "COLORBOX2" }
        +"colormask"
        +"colorseriescycle"
        "convertcolorspec".cmd("model".required, "spec".required, "target model".required, "cmd".required) { "CONVERTCOLORSPEC" }
        "extractcolorspec".cmd(colorArg, "cmd".required) { "EXTRACTCOLORSPEC" }
        "extractcolorspecs".cmd(colorArg, "model-cmd".required, "color-cmd".required) { "EXTRACTCOLORSPECS" }
        "fcolorbox".cmd("model-list".optional, "frame spec-list".required, "background spec-list".required, textArg) { "FCOLORBOX2" }
        "fcolorbox".cmd("frame model-list".optional, "frame spec-list".required, "background model-list".optional, "background spec-list".required, textArg) { "FCOLORBOX3" }
        "fcolorbox".cmd("frame color".required, "background model-list".optional, "background spec-list".required, textArg) { "FCOLORBOX4" }
        +"hiderowcolors"
        "maskcolors".cmd("num model".optional, colorArg) { "MASKCOLORS" }
        "pagecolor".cmd("model-list".optional, colorArg) { "PAGECOLOR2" }
        "resetcolorseries".cmd("div".optional, "name".required) { "RESETCOLORSERIES" }
        "rowcolors".cmd("commands".optional, "row".required, "odd-row color".required, "even-row color".required) { "ROWCOLORS" }
        "rowcolors*".cmd("commands".optional, "row".required, "odd-row color".required, "even-row color".required) { "ROWCOLORS_STAR" }
        +"rownum"
        +"showrowcolors"
        "textcolor".cmd("model-list".optional, colorArg, "text".required)
        +"xglobal"

        packageOf("color")
        "color".cmd(colorArg) { "COLOR_CMD" }
        "colorbox".cmd(colorArg, "text".required) { "COLORBOX" }
        "fcolorbox".cmd("frame color".required, "background color".required, "text".required) { "FCOLORBOX" }
        +"nopagecolor"
        +"normalcolor"
        "pagecolor".cmd(colorArg) { "PAGECOLOR" }
        "textcolor".cmd(colorArg, textArg)
    }

    val captionRelated = buildCommands {
        underPackage("caption") {
            "captionsetup".cmd("type".optional(LatexContexts.Literal), "options".required(LatexContexts.Literal)) {
                "Configure caption formatting globally or for a specific float type."
            }
            "captionof".cmd("float type".required(LatexContexts.Literal), "list entry".optional(LatexContexts.Text), "heading".required(LatexContexts.Text)) {
                "Set a caption outside a float environment."
            }
            "captionlistentry".cmd("type".optional(LatexContexts.Literal), "entry".required(LatexContexts.Text)) {
                "Insert an entry into a caption list without creating a float."
            }
            "ContinuedFloat".cmd("name".optional(LatexContexts.Literal)) {
                "Continue numbering from a previous float."
            }
        }

        underPackage("subcaption") {
            "subcaption".cmd("heading".required(LatexContexts.Text)) {
                "Set a caption for a sub-float."
            }
            "subcaptionbox".cmd(
                "short heading".optional(LatexContexts.Text),
                "heading".required(LatexContexts.Text),
                "width".optional(LatexContexts.Literal),
                "inner-pos".optional(LatexContexts.Position),
                "contents".required(LatexContexts.Text)
            ) {
                "Create a boxed sub-caption with content."
            }
            "subref".cmd(labelArg) {
                "Reference a sub-caption label."
            }
            "subref*".cmd(labelArg) {
                "Reference a sub-caption label without hyperlink."
            }
            "phantomsubcaption".cmd {
                "Step the sub-caption counter without typesetting a caption."
            }
            underContext(LatexContexts.Preamble) {
                "subcaptionsetup".cmd("options".required(LatexContexts.Literal)) {
                    "Configure sub-caption defaults."
                }
                "subcaptionlistentry".cmd("entry".required(LatexContexts.Text)) {
                    "Insert an entry into the sub-caption list."
                }
                "subrefformat".cmd(
                    "labelformat".required(LatexContexts.Literal),
                    "format".required(setOf(LatexContexts.Text, LatexContexts.InsideDefinition))
                ) {
                    "Define how sub-caption references are formatted."
                }
            }
        }
    }
}
