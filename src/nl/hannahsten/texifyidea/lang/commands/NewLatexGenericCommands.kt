package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LContextIntros
import nl.hannahsten.texifyidea.lang.LatexCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

object NewLatexGenericCommands : LatexCommandSet() {
    private val textArg = LArgument.required("text", LatexContexts.TEXT)
    private val labelArg = LArgument.required("label", LatexContexts.LABEL_REF)

    val citation = textCommands {
        val before = "before".optional
        val after = "after".optional
        val keys = required("keys", LatexContexts.BIBTEX_KEY)

        "cite".cmd("extratext".optional, keys) { "CITE" }
        "bibliographystyle".cmd(required("style", LatexContexts.BIB_STYLE)) { "BIBLIOGRAPHYSTYLE" }

        packageOf("natbib")
        "Citealp".cmd(before, after, keys) { "CITEALP_CAPITALIZED" }
        "Citealp*".cmd(before, after, keys) { "CITEALP_STAR_CAPITALIZED" }
        "Citealt".cmd(before, after, keys) { "CITEALT_CAPITALIZED" }
        "Citealt*".cmd(before, after, keys) { "CITEALT_STAR_CAPITALIZED" }
        "Citeauthor".cmd(keys) { "CITEAUTHOR_CAPITALIZED" }
        "Citeauthor*".cmd(keys) { "CITEAUTHOR_STAR_CAPITALIZED" }
        "Citep".cmd(before, after, keys) { "CITEP_CAPITALIZED" }
        "Citep*".cmd(before, after, keys) { "CITEP_STAR_CAPITALIZED" }
        "Citet".cmd(before, after, keys) { "CITET_CAPITALIZED" }
        "Citet*".cmd(before, after, keys) { "CITET_STAR_CAPITALIZED" }
        "citealp".cmd(before, after, keys) { "CITEALP" }
        "citealp*".cmd(before, after, keys) { "CITEALP_STAR" }
        "citealt".cmd(before, after, keys) { "CITEALT" }
        "citealt*".cmd(before, after, keys) { "CITEALT_STAR" }
        "citeauthor".cmd(keys) { "CITEAUTHOR" }
        "citeauthor*".cmd(keys) { "CITEAUTHOR_STAR" }
        "citenum".cmd(keys) { "CITENUM" }
        "citep".cmd(before, after, keys) { "CITEP" }
        "citep*".cmd(before, after, keys) { "CITEP_STAR" }
        "citet".cmd(before, after, keys) { "CITET" }
        "citet*".cmd(before, after, keys) { "CITET_STAR" }
        "citetext".cmd(textArg) { "CITETEXT" }
        "citetitle".cmd(keys) { "CITETITLE" }
        "citetitle*".cmd(keys) { "CITETITLE_STAR" }
        "citeyear".cmd(keys) { "CITEYEAR" }
        "citeyear*".cmd(keys) { "CITEYEAR_STAR" }
        "citeyearpar".cmd(keys) { "CITEYEARPAR" }


        packageOf("biblatex")
        val prenote = "prenote".optional
        val postnote = "postnote".optional
        val key = required("keys", LContextIntros.BIBTEX_KEY)
        val volume = "volume".required
        val page = "page".optional
        "Autocite".cmd(prenote, postnote, key) { "AUTOCITE_CAPITALIZED" }
        "Autocite*".cmd(prenote, postnote, key) { "AUTOCITE_STAR_CAPITALIZED" }
        "Autocites".cmd(prenote, postnote, key) { "AUTOCITES_CAPITALIZED" }
        "Avolcite".cmd(prenote, volume, page, key) { "AVOLCITE_CAPITALIZED" }
        "Avolcites".cmd(prenote, volume, page, key) { "AVOLCITES_CAPITALIZED" }
        "Cite".cmd(prenote, postnote, key) { "CITE_CAPITALIZED" }
        "Citeauthor".cmd(prenote, postnote, key) { "BIBLATEX_CITEAUTHOR_CAPITALIZED" }
        "Citeauthor*".cmd(prenote, postnote, key) { "BIBLATEX_CITEAUTHOR_STAR_CAPITALIZED" }
        "Cites".cmd(prenote, postnote, key) { "CITES_CAPITALIZED" }
        "Ftvolcite".cmd(prenote, volume, page, key) { "FTVOLCITE_CAPITALIZED" }
        "Fvolcite".cmd(prenote, volume, page, key) { "FVOLCITE_CAPITALIZED" }
        "Fvolcites".cmd(prenote, volume, page, key) { "FVOLCITES_CAPITALIZED" }
        "Notecite".cmd(prenote, postnote, key) { "NOTECITE_CAPITALIZED" }
        "Parencite".cmd(prenote, postnote, key) { "PARENCITE_CAPITALIZED" }
        "Parencites".cmd(prenote, postnote, key) { "PARENCITES_CAPITALIZED" }
        "Pnotecite".cmd(prenote, postnote, key) { "PNOTECITE_CAPITALIZED" }
        "Pvolcite".cmd(prenote, volume, page, key) { "PVOLCITE_CAPITALIZED" }
        "Pvolcites".cmd(prenote, volume, page, key) { "PVOLCITES_CAPITALIZED" }
        "Smartcite".cmd(prenote, postnote, key) { "SMARTCITE_CAPITALIZED" }
        "Smartcites".cmd(prenote, postnote, key) { "SMARTCITES_CAPITALIZED" }
        "Svolcite".cmd(prenote, volume, page, key) { "SVOLCITE_CAPITALIZED" }
        "Svolcites".cmd(prenote, volume, page, key) { "SVOLCITES_CAPITALIZED" }
        "Textcite".cmd(prenote, postnote, key) { "TEXTCITE_CAPITALIZED" }
        "Textcites".cmd(prenote, postnote, key) { "TEXTCITES_CAPITALIZED" }
        "Tvolcite".cmd(prenote, volume, page, key) { "TVOLCITE_CAPITALIZED" }
        "Tvolcites".cmd(prenote, volume, page, key) { "TVOLCITES_CAPITALIZED" }
        "Volcite".cmd(prenote, volume, page, key) { "VOLCITE_CAPITALIZED" }
        "Volcites".cmd(prenote, volume, page, key) { "VOLCITES_CAPITALIZED" }
        "autocite".cmd(prenote, postnote, key) { "AUTOCITE" }
        "autocite*".cmd(prenote, postnote, key) { "AUTOCITE_STAR" }
        "autocites".cmd(prenote, postnote, key) { "AUTOCITES" }
        "avolcite".cmd(prenote, volume, page, key) { "AVOLCITE" }
        "avolcites".cmd(prenote, volume, page, key) { "AVOLCITES" }
        "brackettext".cmd(textArg) { "BRACKETTEXT" }
        "cite*".cmd(prenote, postnote, key) { "CITE_STAR" }
        "citeauthor".cmd(prenote, postnote, key) { "BIBLATEX_CITEAUTHOR" }
        "citeauthor*".cmd(prenote, postnote, key) { "BIBLATEX_CITEAUTHOR_STAR" }
        "citedate".cmd(prenote, postnote, key) { "CITEDATE" }
        "citedate*".cmd(prenote, postnote, key) { "CITEDATE_STAR" }
        "cites".cmd(prenote, postnote, key) { "CITES" }
        "citetitle".cmd(prenote, postnote, key) { "BIBLATEX_CITETITLE" }
        "citetitle*".cmd(prenote, postnote, key) { "BIBLATEX_CITETITLE_STAR" }
        "citeurl".cmd(prenote, postnote, key) { "CITEURL" }
        "citeyear".cmd(prenote, postnote, key) { "BIBLATEX_CITEYEAR" }
        "citeyear*".cmd(prenote, postnote, key) { "BIBLATEX_CITEYEAR_STAR" }
        "fnotecite".cmd(prenote, postnote, key) { "FNOTECITE" }
        "footcite".cmd(prenote, postnote, key) { "FOOTCITE" }
        "footcites".cmd(prenote, postnote, key) { "FOOTCITES" }
        "footcitetext".cmd(prenote, postnote, key) { "FOOTCITETEXT" }
        "footcitetexts".cmd(prenote, postnote, key) { "FOOTCITETEXTS" }
        "footfullcite".cmd(prenote, postnote, key) { "FOOTFULLCITE" }
        "ftvolcite".cmd(prenote, volume, page, key) { "FTVOLCITE" }
        "ftvolcites".cmd(prenote, volume, page, key) { "FTVOLCITES" }
        "fullcite".cmd(prenote, postnote, key) { "FULLCITE" }
        "fvolcite".cmd(prenote, volume, page, key) { "FVOLCITE" }
        "fvolcites".cmd(prenote, volume, page, key) { "FVOLCITES" }
        "nocite".cmd(key) { "BIBLATEX_NOCITE" }
        "notecite".cmd(prenote, postnote, key) { "NOTECITE" }
        "parencite".cmd(prenote, postnote, key) { "PARENCITE" }
        "parencite*".cmd(prenote, postnote, key) { "PARENCITE_STAR" }
        "parencites".cmd(prenote, postnote, key) { "PARENCITES" }
        "parenttext".cmd(textArg) { "PARENTTEXT" }
        "pnotecite".cmd(prenote, postnote, key) { "PNOTECITE" }
        "pvolcite".cmd(prenote, volume, page, key) { "PVOLCITE" }
        "pvolcites".cmd(prenote, volume, page, key) { "PVOLCITES" }
        "smartcite".cmd(prenote, postnote, key) { "SMARTCITE" }
        "smartcites".cmd(prenote, postnote, key) { "SMARTCITES" }
        "supercite".cmd(prenote, postnote, key) { "SUPERCITE" }
        "supercites".cmd(prenote, postnote, key) { "SUPERCITES" }
        "svolcite".cmd(prenote, volume, page, key) { "SVOLCITE" }
        "svolcites".cmd(prenote, volume, page, key) { "SVOLCITES" }
        "textcite".cmd(prenote, postnote, key) { "TEXTCITE" }
        "textcites".cmd(prenote, postnote, key) { "TEXTCITES" }
        "tvolcite".cmd(prenote, volume, page, key) { "TVOLCITE" }
        "tvolcites".cmd(prenote, volume, page, key) { "TVOLCITES" }
        "volcite".cmd(prenote, volume, page, key) { "VOLCITE" }
        "volcites".cmd(prenote, volume, page, key) { "VOLCITES" }

    }

    val reference = buildCommands {

        "label".cmd(required("key", LatexContexts.LABEL_DEF)) {
            "Define a label for referencing"
        }

        val label1 = LArgument.required("label1", LatexContexts.LABEL_REF)
        val label2 = LArgument.required("label2", LatexContexts.LABEL_REF)
        "ref".cmd(labelArg) { "Reference to a label" }
        "pageref".cmd(labelArg) { "Page reference to a label" }

        +"refname"

        inPackage("cleveref") {
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

        inPackage("hyperref"){
            "Autoref".cmd(labelArg) { "AUTOREF_CAPITAL" }
            "autoref".cmd(labelArg) { "AUTOREF" }
            "fullref".cmd(labelArg) { "FULLREF" }
            "hyperref".cmd("options".optional, labelArg) { "HYPERREF" }
            "nameref".cmd(labelArg) { "NAMEREF" }

            val urlArg = LArgument.required("url", LatexContexts.URL)
            "href".cmd(urlArg, textArg) { "HREF" }
            "url".cmd(urlArg) { "URL" }
        }

        inPackage("varioref"){
            "Vref".cmd(labelArg) { "VREF_CAPITAL" }
            "vref".cmd(labelArg) { "VREF" }
            "vrefrange".cmd("start".required, "end".required, "text".optional) { "VREFRANGE" }
        }
    }

    val algorithm = buildCommands {
        packageOf("algpseudocode")
        setCommandContext(LatexContexts.ALGORITHMICX)
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
        val label = required("label", LatexContexts.TEXT)
        val insert = "insert".optional

        setCommandContext() // TODO: maybe setCommandContext(LatexContexts.PREAMBLE)
        "loadglsentries".cmd(required("glossariesfile", LatexContexts.SINGLE_FILE))
        "longnewglossaryentry".cmd("name".required, "options".required, "description".required)
        "newabbreviation".cmd(options, "name".required, "short".required, "long".required)
        "newacronym".cmd(options, "name".required, "short".required, "long".required)
        "newglossaryentry".cmd("name".required, "options".required)


        setCommandContext(LatexContexts.TEXT)
        "GLS".cmd(options, label, label)
        "GLSdesc".cmd(options, label, label)
        "GLSfirst".cmd(options, label, label)
        "GLSfirstplural".cmd(options, label, label)
        "GLSname".cmd(options, label, label)
        "GLSplural".cmd(options, label, label)
        "GLSsymbol".cmd(options, label, label)
        "GLStext".cmd(options, label, label)
        "GLSuseri".cmd(options, label, label)
        "GLSuserii".cmd(options, label, label)
        "GLSuseriii".cmd(options, label, label)
        "GLSuseriv".cmd(options, label, label)
        "GLSuserv".cmd(options, label, label)
        "GLSuservi".cmd(options, label, label)
        "Gls".cmd(options, label, label)
        "Glsdesc".cmd(options, label, label)
        "Glsdisp".cmd(options, label, "text".optional)
        "Glsfirst".cmd(options, label, label)
        "Glsfirstplural".cmd(options, label, label)
        "Glslink".cmd(options, label, "text".optional)
        "Glsname".cmd(options, label, label)
        "Glspl".cmd(options, label, label)
        "Glspl".cmd(options, label, label)
        "Glsplural".cmd(options, label, label)
        "Glssymbol".cmd(options, label, label)
        "Glstext".cmd(options, label, label)
        "Glsuseri".cmd(options, label, label)
        "Glsuserii".cmd(options, label, label)
        "Glsuseriii".cmd(options, label, label)
        "Glsuseriv".cmd(options, label, label)
        "Glsuserv".cmd(options, label, label)
        "Glsuservi".cmd(options, label, label)
        "gls".cmd(options, label, label)
        "glsdesc".cmd(options, label, label)
        "glsdisp".cmd(options, label, "text".optional)
        "glsfirst".cmd(options, label, label)
        "glsfirstplural".cmd(options, label, label)
        "glslink".cmd(options, label, "text".optional)
        "glsname".cmd(options, label, label)
        "glspl".cmd(options, label, label)
        "glsplural".cmd(options, label, label)
        "glssymbol".cmd(options, label, label)
        "glstext".cmd(options, label, label)
        "glsuseri".cmd(options, label, label)
        "glsuserii".cmd(options, label, label)
        "glsuseriii".cmd(options, label, label)
        "glsuseriv".cmd(options, label, label)
        "glsuserv".cmd(options, label, label)
        "glsuservi".cmd(options, label, label)


        packageOf("acronym")

        val linebreakPenalty = "linebreak penalty".optional
        val acronym = required("acronym", LatexContexts.TEXT)

        setCommandContext()
        "acro".cmd(acronym, "short name".optional, "full name".required)
        "acrodef".cmd(acronym, "short name".optional, "full name".required)
        "newacro".cmd(acronym, "short name".optional, "full name".required)


        setCommandContext(LatexContexts.TEXT)
        "Ac".cmd(linebreakPenalty, acronym)
        "Ac*".cmd(linebreakPenalty, acronym)
        "Acf".cmd(linebreakPenalty, acronym)
        "Acf*".cmd(linebreakPenalty, acronym)
        "Acfi".cmd(linebreakPenalty, acronym)
        "Acfi*".cmd(linebreakPenalty, acronym)
        "Acfip".cmd(linebreakPenalty, acronym)
        "Acfip*".cmd(linebreakPenalty, acronym)
        "Acfp".cmd(linebreakPenalty, acronym)
        "Acfp*".cmd(linebreakPenalty, acronym)
        "Acl".cmd(linebreakPenalty, acronym)
        "Acl*".cmd(linebreakPenalty, acronym)
        "Aclp".cmd(linebreakPenalty, acronym)
        "Aclp*".cmd(linebreakPenalty, acronym)
        "Aclu".cmd(linebreakPenalty, acronym)
        "Aclu*".cmd(linebreakPenalty, acronym)
        "Acp".cmd(linebreakPenalty, acronym)
        "Acp*".cmd(linebreakPenalty, acronym)
        "Iac".cmd(linebreakPenalty, acronym)
        "Iac*".cmd(linebreakPenalty, acronym)
        "ac".cmd(linebreakPenalty, acronym)
        "ac*".cmd(linebreakPenalty, acronym)
        "acf".cmd(linebreakPenalty, acronym)
        "acf*".cmd(linebreakPenalty, acronym)
        "acfi".cmd(linebreakPenalty, acronym)
        "acfi*".cmd(linebreakPenalty, acronym)
        "acfip".cmd(linebreakPenalty, acronym)
        "acfip*".cmd(linebreakPenalty, acronym)
        "acfp".cmd(linebreakPenalty, acronym)
        "acfp*".cmd(linebreakPenalty, acronym)
        "acl".cmd(linebreakPenalty, acronym)
        "acl*".cmd(linebreakPenalty, acronym)
        "aclp".cmd(linebreakPenalty, acronym)
        "aclp*".cmd(linebreakPenalty, acronym)
        "aclu".cmd(linebreakPenalty, acronym)
        "aclu*".cmd(linebreakPenalty, acronym)
        "acp".cmd(linebreakPenalty, acronym)
        "acp*".cmd(linebreakPenalty, acronym)
        "acs".cmd(linebreakPenalty, acronym)
        "acs*".cmd(linebreakPenalty, acronym)
        "acsp".cmd(linebreakPenalty, acronym)
        "acsp*".cmd(linebreakPenalty, acronym)
        "acsu".cmd(linebreakPenalty, acronym)
        "acsu*".cmd(linebreakPenalty, acronym)
        "iac".cmd(linebreakPenalty, acronym)
        "iac*".cmd(linebreakPenalty, acronym)


    }


    val tcolorboxDefinitionCommands = buildCommands {
        packageOf("tcolorbox")

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
        val textCtx = LatexContexts.TEXT
        val startingCodeRequired = required("starting code", ctx = textCtx)
        val endingCodeRequired = required("ending code", ctx = textCtx)

        "lstnewenvironment".cmd(nameRequired, numberOptional, defaultArgOptional, startingCodeRequired, endingCodeRequired) { "Define a new listings environment" }
    }

    val listings = buildCommands {
        packageOf("listings")
        "lstinputlisting".cmd("options".optional, required("filename", LatexContexts.SINGLE_FILE))
        inPackage("luacode") {
            "directlua".cmd("lua code".required)
            "luaexec".cmd("lua code".required)
        }
        inPackage("pythontex") {
            +"py"
            +"pyb"
            +"pyc"
            +"pys"
            +"pyv"
        }
    }


    val mathtools = buildCommands {
        packageOf("mathtools")
        setCommandContext(LatexContexts.PREAMBLE)
        val cmd = "cmd".required
        val numArgs = "num args".optional
        val leftDelimiter = "left delimiter".required
        val rightDelimiter = "right delimiter".required
        val body = required("body", LatexContexts.TEXT)
        val preCode = "pre code".required
        val postCode = "post code".required
        "DeclarePairedDelimiter".cmd(cmd, leftDelimiter, rightDelimiter) { "Declare a paired delimiter" }
        "DeclarePairedDelimiterX".cmd(cmd, numArgs, leftDelimiter, rightDelimiter) { "Declare a paired delimiter with extended arguments" }
        "DeclarePairedDelimiterXPP".cmd(cmd, numArgs, preCode, leftDelimiter, rightDelimiter, postCode, body) { "Declare a paired delimiter with pre and post code" }

    }


    val colorDefinitionCommands = buildCommands {
        setCommandContext(LatexContexts.PREAMBLE)

        val typeOpt = "type".optional
        val nameReq = "name".required
        val modelListReq = required("model-list", LatexContexts.LITERAL)
        val specListReq = required("spec-list", LatexContexts.LITERAL)

        packageOf("color")
        "definecolor".cmd(typeOpt, nameReq, modelListReq, specListReq) { "Define a color" }
        "DefineNamedColor".cmd("type".required, nameReq, modelListReq, specListReq) { "Define a named color" }

        packageOf("xcolor")
        "providecolor".cmd(typeOpt, nameReq, modelListReq, specListReq) { "Provide a color" }
        "colorlet".cmd(typeOpt, nameReq, optional("num model"), "color".required) { "Define a color based on another" }
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
            optional("b-model"),
            "b-spec".required,
            "s-model".required,
            "s-spec".required
        ) { "Define a color series" }
    }

    val genericCommands = buildCommands {
        val titleArg = LArgument.required("title", LatexContexts.TEXT)

        packageOf("")
        symbol("LaTeX", "LaTeX")
        symbol("LaTeXe", "LaTeX2ε")
        symbol("$", "$")
        symbol("AA", "Å")
        symbol("AE", "Æ")
        +"Huge"
        +"LARGE"
        +"Large"

        symbol("OE", "Œ")
        "Roman".cmd("counter".required) { "CAPITAL_ROMAN" }
        symbol("S", "§")
        symbol("aa", "å")
        "addtocounter".cmd("countername".required, "value".required) { "ADDTOCOUNTER" }

        symbol("ae", "æ")
        +"appendix"
        "author".cmd("name".required) { "AUTHOR" }
        +"baselineskip"
        +"baselinestretch"
        +"bf"
        +"bfseries"
        "bibitem".cmd("label".optional, "citekey".required) { "BIBITEM" }



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
        symbol("dag", "†")
        "date".cmd(textArg) { "DATE" }
        symbol("ddag", "‡")
        +"def"
        +"dotfill"
        +"em"
        "emph".cmd(textArg) { "EMPH" }

        "enlargethispage".cmd("size".required) { "ENLARGETHISPAGE" }
        "enlargethispage*".cmd("size".required) { "ENLARGETHISPAGE_STAR" }
        "ensuremath".cmd(textArg) { "ENSUREMATH" }
        +"evensidemargin"

        +"family"
        "fbox".cmd(textArg) { "FBOX" }
        "figurename".cmd("name".required) { "FIGURENAME" }
        symbol("flq", "‹")
        symbol("flqq", "«")
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
        symbol("frq", "›")
        symbol("frqq", "»")
        "glossary".cmd(textArg) { "GLOSSARY" }
        "glossaryentry".cmd(textArg, "pagenum".required) { "GLOSSARYENTRY" }
        symbol("glq", ",")
        symbol("glqq", "„")
        symbol("grq", "‘")
        symbol("grqq", "“")
        +"hfill"
        +"hrule"
        +"hrulefill"
        "hspace".cmd("length".required) { "HSPACE" }
        "hspace*".cmd("length".required) { "HSPACE_STAR" }
        +"hss"
        +"huge"
        "hyphenation".cmd("words".required) { "HYPHENATION" }
        symbol("i", "i (dotless)")
        "ifthenelse".cmd("test".required, "then clause".required, "else clause".required) { "IFTHENELSE" }

        "indexname".cmd("name".required) { "INDEXNAME" }
        +"indexspace"
        "intex".cmd("entry".required) { "INDEX" }
        +"it"
        "item".cmd("label".optional) { "ITEM" }
        +"itshape"

        +"large"
        symbol("lbrack", "[")
        symbol("ldots", "…")
        +"lefteqn"
        +"let"
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
        symbol("lq", "‘")
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
        "newtheorem".cmd("envname".required, "numberedlike".optional, "caption".required, "within".optional) { "NEWTHEOREM" }
        "newtheorem*".cmd("envname".required, "caption".required) { "NEWTHEOREM_STAR" }
        "nocite".cmd("keys".required) { "NOCITE" }
        +"nofiles"
        "nolinebreak".cmd("number".optional) { "NOLINEBREAK" }
        +"nonumber"
        "nopagebreak".cmd("number".optional) { "NOPAGEBREAK" }
        +"normalfont"
        +"normalsize"
        "oarg".cmd("arg".required) { "OARG" }
        +"oddsidemargin"
        symbol("oe", "œ")
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
        symbol("pounds", "£")
        +"printindex"
        symbol("r", "˚ (accent)")
        symbol("rbrack", "]")

        +"righthyphenmin"
        +"rightmargin"
        +"rightmark"
        +"rm"
        +"rmfamily"
        "roman".cmd("counter".required) { "ROMAN" }
        symbol("rq", "’")
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
        +"subinputfrom"
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
        symbol("textasciicircum", "^")
        symbol("textasciitilde", "~")
        symbol("textasteriskcentered", "⁎")
        symbol("textbackslash", "\\")
        symbol("textbar", "|")
        "textbf".cmd(textArg) { "TEXTBF" }
        symbol("textbraceleft", "{")
        symbol("textbraceright", "}")
        symbol("textbullet", "•")
        "textcircled".cmd("a".required) { "CIRCLED_TEXT" }
        symbol("textcopyright", "©")
        symbol("textdagger", "†")
        symbol("textdaggerdbl", "‡")
        symbol("textdollar", "$")
        symbol("textellipsis", "…")
        symbol("textemdash", "—")
        symbol("textendash", "–")
        symbol("textexclamdown", "¡")
        symbol("textgreater", ">")
        +"textheight"
        "textit".cmd(textArg) { "TEXTIT" }
        symbol("textless", "<")
        "textlf".cmd(textArg) { "TEXTLF" }
        "textmd".cmd(textArg) { "TEXTMD" }
        +"textnormal"
        +"textparagraph"
        symbol("textperiodcentered", "·")
        symbol("textquestiondown", "¿")
        symbol("textquotedblleft", "“")
        symbol("textquotedblright", "”")
        symbol("textquoteleft", "‘")
        symbol("textquoteright", "’")
        symbol("textregistered", "®")
        "textrm".cmd(textArg) { "TEXTRM" }
        "textsc".cmd("textsc".required) { "TEXTSC" }
        symbol("textsection", "§")
        "textsf".cmd(textArg) { "TEXTSF" }
        "textsl".cmd(textArg) { "TEXTSL" }
        symbol("textsterling", "£")
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
        symbol("vdots", "⋮")
        +"vline"
        "vspace".cmd("length".required) { "VSPACE" }
        "vspace*".cmd("length".required) { "VSPACE_STAR" }
        +"width"

        packageOf("biblatex")
        +"printbibliography"



        packageOf("amsmath")
        underCmdContext(LatexContexts.PREAMBLE) {
            "DeclareMathOperator".cmd("command".required, "operator".required) { "DECLARE_MATH_OPERATOR" }
        }
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

        packageOf("textcomp")
        symbol("textdownarrow", "↓")
        symbol("textleftarrow", "←")
        symbol("textrightarrow", "→")
        symbol("textuparrow", "↑")

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



    val colorCommands = buildCommands {

        packageOf("xcolor")
        "blendcolors".cmd("mix expr".required) { "BLENDCOLORS" }
        "blendcolors*".cmd("mix expr".required) { "BLENDCOLORS_STAR" }
        "boxframe".cmd("width".required, "height".required, "depth".required) { "BOXFRAME" }
        "color".cmd("model-list".optional, "spec-list".required) { "COLOR2" }
        "colorbox".cmd("model-list".optional, "spec-list".required, textArg) { "COLORBOX2" }
        +"colormask"
        +"colorseriescycle"
        "convertcolorspec".cmd("model".required, "spec".required, "target model".required, "cmd".required) { "CONVERTCOLORSPEC" }
        "extractcolorspec".cmd("color".required, "cmd".required) { "EXTRACTCOLORSPEC" }
        "extractcolorspecs".cmd("color".required, "model-cmd".required, "color-cmd".required) { "EXTRACTCOLORSPECS" }
        "fcolorbox".cmd("model-list".optional, "frame spec-list".required, "background spec-list".required, textArg) { "FCOLORBOX2" }
        "fcolorbox".cmd("frame model-list".optional, "frame spec-list".required, "background model-list".optional, "background spec-list".required, textArg) { "FCOLORBOX3" }
        "fcolorbox".cmd("frame color".required, "background model-list".optional, "background spec-list".required, textArg) { "FCOLORBOX4" }
        +"hiderowcolors"
        "maskcolors".cmd("num model".optional, "color".required) { "MASKCOLORS" }
        "pagecolor".cmd("model-list".optional, "spec-list".required) { "PAGECOLOR2" }
        "resetcolorseries".cmd("div".optional, "name".required) { "RESETCOLORSERIES" }
        "rowcolors".cmd("commands".optional, "row".required, "odd-row color".required, "even-row color".required) { "ROWCOLORS" }
        "rowcolors*".cmd("commands".optional, "row".required, "odd-row color".required, "even-row color".required) { "ROWCOLORS_STAR" }
        +"rownum"
        +"showrowcolors"
        "testcolor".cmd("color".required) { "TESTCOLOR" }
        "testcolor".cmd("model-list".optional, "spec-list".required) { "TESTCOLOR2" }
        "textcolor".cmd("model-list".optional, "spec-list".required, textArg) { "TEXTCOLOR2" }
        +"xglobal"

        packageOf("color")
        "color".cmd("color".required) { "COLOR_CMD" }
        "colorbox".cmd("color".required, textArg) { "COLORBOX" }
        "fcolorbox".cmd("frame color".required, "background color".required, textArg) { "FCOLORBOX" }
        +"nopagecolor"
        +"normalcolor"
        "pagecolor".cmd("color".required) { "PAGECOLOR" }
        "textcolor".cmd("color".required, textArg) { "TEXTCOLOR" }
    }
}