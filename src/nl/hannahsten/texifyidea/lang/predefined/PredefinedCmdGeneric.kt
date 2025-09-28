package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

object PredefinedCmdGeneric : PredefinedCommandSet() {
    private val textArg = LArgument.required("text", LatexContexts.Text)
    private val labelArg = LArgument.required("label", LatexContexts.LabelReference)

    val genericCommands = buildCommands {
        val titleArg = LArgument.required("title", LatexContexts.Text)

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

        "Roman".cmd("counter".required) { "CAPITAL_ROMAN" }

        "addtocounter".cmd("countername".required, "value".required) { "ADDTOCOUNTER" }
        +"appendix"
        "author".cmd("name".required) { "AUTHOR" }
        +"baselineskip"
        +"baselinestretch"
        +"bf"
        +"bfseries"
        "bibitem".cmd("label".optional, "citekey".required(LatexContexts.BibKey))

        +"bigskip"
        +"boldmath"
        "caption".cmd("shorttext".optional, textArg) { "CAPTION" }
        "captionof".cmd("float type".required, "list entry".optional, "heading".required) { "CAPTIONOF" }
        "chapter".cmd("shorttitle".optional, titleArg) { "CHAPTER" }
        "chapter*".cmd(titleArg) { "CHAPTER_STAR" }

        +"cleardoublepage"
        +"clearpage"
        +"columnsep "
        +"columnwidth"
        "contentsline".cmd("type".required, textArg, "page".required) { "CONTENTSLINE" }
        "contentsname".cmd("name".required) { "CONTENTSNAME" }

        "date".cmd(textArg) { "DATE" }

        +"dotfill"
        +"em"
        "emph".cmd(textArg) { "EMPH" }

        "enlargethispage".cmd("size".required) { "ENLARGETHISPAGE" }
        "enlargethispage*".cmd("size".required) { "ENLARGETHISPAGE_STAR" }
        +"evensidemargin"

        +"family"
        "fbox".cmd(textArg) { "FBOX" }
        "figurename".cmd("name".required) { "FIGURENAME" }

        +"flushbottom"
        +"flushleft"
        +"flushright"
        "fontencoding".cmd("enc".required) { "FONTENCODING" }
        "fontfamily".cmd("family".required) { "FONTFAMILY" }
        "fontseries".cmd("series".required) { "FONTSERIES" }
        "fontshape".cmd("shape".required) { "FONTSHAPE" }
        "fontsize".cmd("size".required, "skip".required) { "FONTSIZE" }
        "footnote".cmd("number".optional, textArg) { "FOOTNOTE" }
        +"footnotemark"
        +"footnotesize"
        "footnotetext".cmd("number".optional, textArg) { "FOOTNOTETEXT" }
        "frame".cmd(textArg) { "FRAME" }
        "framebox".cmd("width".optional, "pos".optional, "text".optional) { "FRAMEBOX" }

        "glossary".cmd(textArg) { "GLOSSARY" }
        "glossaryentry".cmd(textArg, "pagenum".required) { "GLOSSARYENTRY" }

        +"hfill"
        +"hrule"
        +"hrulefill"
        "hspace".cmd("length".required) { "HSPACE" }
        "hspace*".cmd("length".required) { "HSPACE_STAR" }
        +"hss"
        +"huge"
        "hyphenation".cmd("words".required) { "HYPHENATION" }

        "ifthenelse".cmd("test".required, "then clause".required, "else clause".required) { "IFTHENELSE" }

        "indexname".cmd("name".required) { "INDEXNAME" }
        +"indexspace"
        "intex".cmd("entry".required) { "INDEX" }
        +"it"
        "item".cmd("label".optional(LatexContexts.Text)) { "ITEM" }
        +"itshape"

        +"large"
        +"lefteqn"
        +"lfseries"
        "linebreak".cmd("number".optional) { "LINEBREAK" }
        "linethickness".cmd("dimension".required) { "LINETHICKNESS" }
        +"linewidth"
        "listfigurename".cmd("name".required) { "LISTFIGURENAME" }
        +"listfiles"
        +"listoffigures"
        +"listoftables"
        "listtablename".cmd("name".required) { "LISTTABLENAME" }
        "lowercase".cmd(textArg) { "LOWERCASE" }

        +"makeglossary"
        +"makeindex"
        +"makelabel"
        "makelabels".cmd("number".required) { "MAKELABELS" }
        +"maketitle"
        "marg".cmd("arg".required) { "MARG" }
        "mbox".cmd(textArg) { "MBOX" }
        +"mdseries"
        +"medskip"
        "meta".cmd("arg".required) { "META" }
        "multicolumn".cmd("cols".required, "pos".required, textArg) { "MULTICOLUMN" }
        +"newlabel"
        "newlength".cmd("length".required) { "NEWLENGTH" }
        +"newline"
        +"newpage"
        "nocite".cmd("keys".required) { "NOCITE" }
        +"nofiles"
        "nolinebreak".cmd("number".optional) { "NOLINEBREAK" }
        +"nonumber"
        "nopagebreak".cmd("number".optional) { "NOPAGEBREAK" }
        +"normalfont"
        +"normalsize"
        "oarg".cmd("arg".required) { "OARG" }
        +"oddsidemargin"

        "oldstylenums".cmd("number".required) { "OLDSTYLEENUMS" }
        +"onecolumn"
        "onlyifstandalone".cmd("code".required) { "ONLYIFSTANDALONE" }
        "pagebreak".cmd("number".optional) { "PAGEBREAK" }
        +"pagename"
        "pagenumbering".cmd("numstyle".required) { "PAGENUMBERING" }

        "pagestyle".cmd("style".required) { "PAGESTYLE" }
        +"pagetotal"
        +"paperheight"
        +"paperwidth"
        "paragraph".cmd("shorttitle".optional, titleArg) { "PARAGRAPH" }
        "paragraph*".cmd(titleArg) { "PARAGRAPH_STAR" }
        +"paragraphmark"
        "parbox".cmd("pos".optional, "width".required, textArg) { "PARBOX" }
        "parg".cmd("arg".required) { "PARG" }
        +"parindent"
        +"parskip"
        "part".cmd("shorttitle".optional, titleArg) { "PART" }
        "part*".cmd(titleArg) { "PART_STAR" }
        "partname".cmd("name".required) { "PARTNAME" }
        "pdfinfo".cmd("info".required) { "PDFINFO" }

        +"printindex"

        +"righthyphenmin"
        +"rightmargin"
        +"rightmark"
        +"rm"
        +"rmfamily"
        "roman".cmd("counter".required) { "ROMAN" }

        "rule".cmd("line".optional, "width".required, "thickness".required) { "RULE" }
        +"samepage"
        "sbox".cmd("cmd".required, "length".required) { "SBOX" }
        +"sc"
        +"scriptsize"
        +"scshape"
        "section".cmd("shorttitle".optional, titleArg) { "SECTION" }
        "section*".cmd(titleArg) { "SECTION_STAR" }
        +"selectfont"
        "setcounter".cmd("countername".required, "value".required) { "SETCOUNTER" }
        "setlength".cmd("cmd".required, "length".required) { "SETLENGTH" }
        +"sf"
        +"sffamily"
        "shortstack".cmd("pos".optional, textArg) { "SHORTSTACK" }
        +"sl"
        +"slshape"
        +"small"
        +"smallskip"
        +"smash"
        +"space"
        "stepcounter".cmd("counter".required) { "STEPCOUNTER" }
        +"stop"
        "stretch".cmd("factor".required) { "STRETCH" }
        +"subitem"
        "subparagraph".cmd("shorttitle".optional, titleArg) { "SUBPARAGRAPH" }
        "subparagraph*".cmd(titleArg) { "SUBPARAGRAPH_STAR" }
        "subparagraphmark".cmd("code".required) { "SUBPARAGRAPHMARK" }
        "subsection".cmd("shorttitle".optional, titleArg) { "SUBSECTION" }
        "subsection*".cmd(titleArg) { "SUBSECTION_STAR" }
        "subsectionmark".cmd("code".required) { "SUBSECTIONMARK" }
        +"subsubitem"
        "subsubsection".cmd("shorttitle".optional, titleArg) { "SUBSUBSECTION" }
        "subsubsection*".cmd(titleArg) { "SUBSUBSECTION_STAR" }
        "subsubsectionmark".cmd("code".required) { "SUBSUBSECTIONMARK" }
        "suppressfloats".cmd("placement".optional) { "SUPPRESSFLOATS" }
        "symbol".cmd("n".required) { "SYMBOL" }
        +"tabcolsep"
        "tablename".cmd("name".required) { "TABLENAME" }
        +"tableofcontents"

        "textbf".cmd(textArg) { "TEXTBF" }

        "textcircled".cmd("a".required) { "CIRCLED_TEXT" }

        +"textheight"
        "textit".cmd(textArg) { "TEXTIT" }

        "textlf".cmd(textArg) { "TEXTLF" }
        "textmd".cmd(textArg) { "TEXTMD" }
        +"textnormal"
        +"textparagraph"

        "textrm".cmd(textArg) { "TEXTRM" }
        "textsc".cmd("textsc".required) { "TEXTSC" }

        "textsf".cmd(textArg) { "TEXTSF" }
        "textsl".cmd(textArg) { "TEXTSL" }

        "textsubscript".cmd(textArg) { "TEXTSUBSCRIPT" }
        "textsuperscript".cmd(textArg) { "TEXTSUPERSCRIPT" }
        symbol("texttrademark", "™")
        "texttt".cmd(textArg) { "TEXTTT" }
        symbol("textunderscore", "_")
        "textup".cmd(textArg) { "TEXTUP" }
        symbol("textvisiblespace", "␣")
        +"textwidth"
        "thanks".cmd("to".required) { "THANKS" }
        +"thicklines"
        +"thinlines"
        "thispagestyle".cmd("style".required) { "THISPAGESTYLE" }
        +"time"
        +"tiny"
        "title".cmd(textArg) { "TITLE" }
        +"today"
        +"topmargin"
        +"tt"
        +"ttfamily"
        "twocolumn".cmd("text".optional) { "TWOCOLUMN" }
        +"unboldmath"
        "underline".cmd(textArg) { "UNDERLINE" }
        +"unitlength"
        "uppercase".cmd(textArg) { "UPPERCASE" }
        +"upshape"
        "usepgfplotslibrary".cmd("libraries".required) { "USEPGFPLOTSLIBRARY" }
        "usetikzlibrary".cmd("libraries".required) { "USETIKZLIBRARY" }
        +"vline"
        "vspace".cmd("length".required) { "VSPACE" }
        "vspace*".cmd("length".required) { "VSPACE_STAR" }
        +"width"

        packageOf("biblatex")
        +"printbibliography"

        packageOf("amsmath")
        "eqref".cmd(labelArg) { "EQREF" }

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
        "sout".cmd("strikethroughtext".required) { "SOUT" }

        packageOf("ntheorem")
        "thref".cmd(labelArg) { "THREF" }

        packageOf("fontspec")
        "addfontfeature".cmd("font features".required) { "ADDFONTFEATURE" }
        "addfontfeatures".cmd("font features".required) { "ADDFONTFEATURES" }
        "defaultfontfeatures".cmd("font names".optional, "font features".required) { "DEFAULTFONTFEATURES" }
        "fontspec".cmd("font".required, "font features".optional) { "FONTSPEC_CMD" }
        "setmainfont".cmd("font".required, "font features".optional) { "SETMAINFONT" }
        "setmonofont".cmd("font".required, "font features".optional) { "SETMONOFONT" }
        "setsansfont".cmd("font".required, "font features".optional) { "SETSANSFONT" }
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
    }

    val reference = buildCommands {

        "label".cmd("key".required(LatexContexts.LabelDefinition)) {
            "Define a label for referencing"
        }

        val label1 = LArgument.required("label1", LatexContexts.LabelReference)
        val label2 = LArgument.required("label2", LatexContexts.LabelReference)
        "ref".cmd(labelArg) { "Reference to a label" }
        "pageref".cmd(labelArg) { "Page reference to a label" }

        +"refname"

        underPackage("nameref") {
            "nameref".cmd(labelArg) { "Reference to a label" }
            "Nameref".cmd(labelArg) { "Reference to a label with page number" }
        }

        underPackage("cleveref") {
            "Cpageref".cmd(labelArg) { "CPAGEREF_CAPITAL" }
            "Cpagerefrange".cmd(label1, label2) { "CPAGEREFRANGE_CAPITAL" }
            "Cref".cmd(labelArg) { "CREF_CAPITAL" }
            "cpageref".cmd(labelArg) { "CPAGEREF" }
            "cpagerefrange".cmd(label1, label2) { "CPAGEREFRANGE" }
            "cref".cmd(labelArg) { "CREF" }
            "crefrange".cmd(label1, label2) { "CREFRANGE" }
            "labelcpageref".cmd(labelArg) { "LABELCPAGEREF" }
            "labelcref".cmd(labelArg) { "LABELCREF" }
            "lcnamecref".cmd(labelArg) { "LCNAMECREF" }
            "lcnamecrefs".cmd(labelArg) { "LCNAMECREFS" }
            "nameCref".cmd(labelArg) { "NAMECREF_CAPITAL" }
            "nameCrefs".cmd(labelArg) { "NAMECREFS_CAPITAL" }
            "namecref".cmd(labelArg) { "NAMECREF" }
            "namecrefs".cmd(labelArg) { "NAMECREFS" }
        }

        underPackage("hyperref") {
            "Autoref".cmd(labelArg) { "AUTOREF_CAPITAL" }
            "autoref".cmd(labelArg) { "AUTOREF" }
            "fullref".cmd(labelArg) { "FULLREF" }
            "hyperref".cmd("options".optional, labelArg) { "HYPERREF" }

            val urlArg = LArgument.required("url", LatexContexts.URL)
            "href".cmd(urlArg, textArg) { "HREF" }
            "url".cmd(urlArg) { "URL" }
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
        packageOf("glossaries")

        val options = "options".optional
        val label = "label".required(setOf(LatexContexts.Text, LatexContexts.GlossaryLabel))
        val insert = "insert".optional

        underContext(LatexContexts.Preamble) {
//            "loadglsentries".cmd("glossariesfile".required(LatexContexts.SingleFile))
            "longnewglossaryentry".cmd("name".required, "options".required, "description".required)
            "newabbreviation".cmd(options, "name".required, "short".required, "long".required)
            "newacronym".cmd(options, "name".required, "short".required, "long".required)
            "newglossaryentry".cmd("name".required, "options".required)
        }

        applicableIn(LatexContexts.Text)
        "GLS".cmd(options, label, insert)
        "GLSdesc".cmd(options, label, insert)
        "GLSfirst".cmd(options, label, insert)
        "GLSfirstplural".cmd(options, label, insert)
        "GLSname".cmd(options, label, insert)
        "GLSplural".cmd(options, label, insert)
        "GLSsymbol".cmd(options, label, insert)
        "GLStext".cmd(options, label, insert)
        "GLSuseri".cmd(options, label, insert)
        "GLSuserii".cmd(options, label, insert)
        "GLSuseriii".cmd(options, label, insert)
        "GLSuseriv".cmd(options, label, insert)
        "GLSuserv".cmd(options, label, insert)
        "GLSuservi".cmd(options, label, insert)
        "Gls".cmd(options, label, insert)
        "Glsdesc".cmd(options, label, insert)
        "Glsdisp".cmd(options, label, "text".optional)
        "Glsfirst".cmd(options, label, insert)
        "Glsfirstplural".cmd(options, label, insert)
        "Glslink".cmd(options, label, "text".optional)
        "Glsname".cmd(options, label, insert)
        "Glspl".cmd(options, label, insert)
        "Glspl".cmd(options, label, insert)
        "Glsplural".cmd(options, label, insert)
        "Glssymbol".cmd(options, label, insert)
        "Glstext".cmd(options, label, insert)
        "Glsuseri".cmd(options, label, insert)
        "Glsuserii".cmd(options, label, insert)
        "Glsuseriii".cmd(options, label, insert)
        "Glsuseriv".cmd(options, label, insert)
        "Glsuserv".cmd(options, label, insert)
        "Glsuservi".cmd(options, label, insert)
        "gls".cmd(options, label, insert)
        "glsdesc".cmd(options, label, insert)
        "glsdisp".cmd(options, label, "text".optional)
        "glsfirst".cmd(options, label, insert)
        "glsfirstplural".cmd(options, label, insert)
        "glslink".cmd(options, label, "text".optional)
        "glsname".cmd(options, label, insert)
        "glspl".cmd(options, label, insert)
        "glsplural".cmd(options, label, insert)
        "glssymbol".cmd(options, label, insert)
        "glstext".cmd(options, label, insert)
        "glsuseri".cmd(options, label, insert)
        "glsuserii".cmd(options, label, insert)
        "glsuseriii".cmd(options, label, insert)
        "glsuseriv".cmd(options, label, insert)
        "glsuserv".cmd(options, label, insert)
        "glsuservi".cmd(options, label, insert)

        packageOf("acronym")
        val linebreakPenalty = "linebreak penalty".optional
        val acronym = "acronym".required(setOf(LatexContexts.Text, LatexContexts.GlossaryLabel))
        underContext(LatexContexts.Preamble) {
            "acro".cmd(acronym, "short name".optional, "full name".required)
            "acrodef".cmd(acronym, "short name".optional, "full name".required)
            "newacro".cmd(acronym, "short name".optional, "full name".required)
        }

        applicableIn(LatexContexts.Text)
        arrayOf(
            "Ac", "Ac*", "Acf", "Acf*", "Acfi", "Acfi*", "Acfip", "Acfip*", "Acfp", "Acfp*",
            "Acl", "Acl*", "Aclp", "Aclp*", "Aclu", "Aclu*", "Acp", "Acp*", "Iac", "Iac*",
            "ac", "ac*", "acf", "acf*", "acfi", "acfi*", "acfip", "acfip*", "acfp", "acfp*",
            "acl", "acl*", "aclp", "aclp*", "aclu", "aclu*", "acp", "acp*", "acs", "acs*",
            "acsp", "acsp*", "acsu", "acsu*", "iac", "iac*"
        ).forEach { it.cmd(linebreakPenalty, acronym) }
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

        val nameRequired = "name".required
        val numberOptional = "number".optional
        val defaultArgOptional = "default arg".optional
        val textCtx = LatexContexts.Text
        val startingCodeRequired = "starting code".required(ctx = textCtx)
        val endingCodeRequired = "ending code".required(ctx = textCtx)

        "lstnewenvironment".cmd(nameRequired, numberOptional, defaultArgOptional, startingCodeRequired, endingCodeRequired) { "Define a new listings environment" }
    }

    val listings = buildCommands {
        underPackage("luacode") {
            "directlua".cmd("lua code".required)
            "luaexec".cmd("lua code".required)
        }
        underPackage("pythontex") {
            +"py"
            +"pyb"
            +"pyc"
            +"pys"
            +"pyv"
        }
    }

    val colorRelatedCommands = buildCommands {
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
}