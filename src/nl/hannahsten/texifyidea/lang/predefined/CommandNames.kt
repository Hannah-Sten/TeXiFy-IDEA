package nl.hannahsten.texifyidea.lang.predefined

/**
 * Names of common LaTeX commands.
 * Note that leading backslash is included in the names.
 */
object CommandNames {
    // 1. Macro definition related
    const val NEW_COMMAND = "\\newcommand"
    const val NEW_COMMAND_STAR = "\\newcommand*"
    const val NEW_COMMAND_X = "\\newcommandx"
    const val PROVIDE_COMMAND = "\\providecommand"
    const val PROVIDE_COMMAND_STAR = "\\providecommand*"
    const val PROVIDE_COMMAND_X = "\\providecommandx"
    const val RENEW_COMMAND = "\\renewcommand"
    const val RENEW_COMMAND_STAR = "\\renewcommand*"
    const val RENEW_COMMAND_X = "\\renewcommandx"
    const val DEF = "\\def"
    const val LET = "\\let"
    const val NEW_IF = "\\newif"
    const val NEW_DOCUMENT_COMMAND = "\\NewDocumentCommand"
    const val PROVIDE_DOCUMENT_COMMAND = "\\ProvideDocumentCommand"
    const val DECLARE_DOCUMENT_COMMAND = "\\DeclareDocumentCommand"
    const val DECLARE_ROBUST_COMMAND_X = "\\DeclareRobustCommandx"
    const val DECLARE_MATH_OPERATOR = "\\DeclareMathOperator"
    const val DECLARE_PAIRED_DELIMITER = "\\DeclarePairedDelimiter"
    const val DECLARE_PAIRED_DELIMITER_X = "\\DeclarePairedDelimiterX"
    const val DECLARE_PAIRED_DELIMITER_XPP = "\\DeclarePairedDelimiterXPP"
    const val CAT_CODE = "\\catcode"

    // 2. Environment definition related
    const val NEW_ENVIRONMENT = "\\newenvironment"
    const val NEW_ENVIRONMENT_X = "\\newenvironmentx"
    const val RENEW_ENVIRONMENT = "\\renewenvironment"
    const val RENEW_ENVIRONMENT_X = "\\renewenvironmentx"
    const val NEW_DOCUMENT_ENVIRONMENT = "\\NewDocumentEnvironment"
    const val PROVIDE_DOCUMENT_ENVIRONMENT = "\\ProvideDocumentEnvironment"
    const val DECLARE_DOCUMENT_ENVIRONMENT = "\\DeclareDocumentEnvironment"
    const val NEW_TCOLORBOX = "\\newtcolorbox"
    const val DECLARE_TCOLORBOX = "\\DeclareTColorBox"
    const val NEW_TCOLORBOX_CAP = "\\NewTColorBox"
    const val PROVIDE_TCOLORBOX = "\\ProvideTColorBox"
    const val RENEW_TCOLORBOX = "\\newtcolorbox"
    const val RENEW_TCOLORBOX_CAP = "\\ReNewTColorBox"
    const val LST_NEW_ENVIRONMENT = "\\lstnewenvironment"
    const val NEW_THEOREM = "\\newtheorem"

    // 3. Class/package related
    const val DOCUMENT_CLASS = "\\documentclass"
    const val USE_PACKAGE = "\\usepackage"
    const val PROVIDES_CLASS = "\\ProvidesClass"
    const val PROVIDES_PACKAGE = "\\ProvidesPackage"
    const val REQUIRE_PACKAGE = "\\RequirePackage"
    const val LOAD_CLASS = "\\LoadClass"
    const val LOAD_CLASS_WITH_OPTIONS = "\\LoadClassWithOptions"
    const val INCLUDE_ONLY = "\\includeonly"
    const val ADD_TO_LUATEX_PATH = "\\addtoluatexpath"
    const val USE_TIKZ_LIBRARY = "\\usetikzlibrary"
    const val USE_PGF_PLOTS_LIBRARY = "\\usepgfplotslibrary"

    // 4. File/input related
    const val INPUT = "\\input"
    const val INCLUDE = "\\include"
    const val INCLUDE_STANDALONE = "\\includestandalone"
    const val SUBFILE_INCLUDE = "\\subfileinclude"
    const val SUBFILE = "\\subfile"
    const val BIBLIOGRAPHY = "\\bibliography"
    const val ADD_BIB_RESOURCE = "\\addbibresource"
    const val EXTERNAL_DOCUMENT = "\\externaldocument"
    const val SVG_PATH = "\\svgpath"
    const val GRAPHICS_PATH = "\\graphicspath"
    const val INCLUDE_FROM = "\\includefrom"
    const val INPUT_FROM = "\\inputfrom"
    const val IMPORT = "\\import"
    const val SUB_IMPORT = "\\subimport"
    const val SUB_INPUT_FROM = "\\subinputfrom"
    const val SUB_INCLUDE_FROM = "\\subincludefrom"

    // 5. TikZ/graphics related
    const val INCLUDE_GRAPHICS = "\\includegraphics"
    const val DECLARE_GRAPHICS_EXTENSIONS = "\\DeclareGraphicsExtensions"
    const val TIKZ_FIG = "\\tikzfig"
    const val C_TIKZ_FIG = "\\ctikzfig"

    // 6. Counter/section related
    const val ADD_TO_COUNTER = "\\addtocounter"
    const val SET_COUNTER = "\\setcounter"
    const val BEGIN = "\\begin"
    const val END = "\\end"
    const val LABEL = "\\label"
    const val PARAGRAPH = "\\paragraph"
    const val SUB_PARAGRAPH = "\\subparagraph"
    const val PART = "\\part"
    const val SECTION = "\\section"
    const val SUB_SECTION = "\\subsection"
    const val SUB_SUB_SECTION = "\\subsubsection"
    const val CHAPTER = "\\chapter"

    // 7. Text style related
    const val RM = "\\rm"
    const val SF = "\\sf"
    const val TT = "\\tt"
    const val IT = "\\it"
    const val SL = "\\sl"
    const val SC = "\\sc"
    const val BF = "\\bf"
    const val TEXT_RM = "\\textrm"
    const val TEXT_SF = "\\textsf"
    const val TEXT_TT = "\\texttt"
    const val TEXT_IT = "\\textit"
    const val TEXT_SL = "\\textsl"
    const val TEXT_SC = "\\textsc"
    const val TEXT_BF = "\\textbf"
    const val EMPH = "\\emph"
    const val TEXT_UP = "\\textup"
    const val TEXT_MD = "\\textmd"
    const val UNDERLINE = "\\underline"
    const val SOUT = "\\sout"
    const val OVERLINE = "\\overline"

    // 8. Math/vertical space related
    const val FRAC = "\\frac"
    const val DFRAC = "\\dfrac"
    const val SQRT = "\\sqrt"
    const val SUM = "\\sum"
    const val INT = "\\int"
    const val IINT = "\\iint"
    const val IIINT = "\\iiint"
    const val IIIINT = "\\iiiint"
    const val PROD = "\\prod"
    const val BIGCUP = "\\bigcup"
    const val BIGCAP = "\\bigcap"
    const val BIGSQCUP = "\\bigsqcup"
    const val BIGVEE = "\\bigvee"
    const val BIGWEDGE = "\\bigwedge"
    const val TEXT = "\\text"
    const val INTERTEXT = "\\intertext"

    // 9. Listings and captions
    const val LSTINPUTLISTING = "\\lstinputlisting"
    const val CAPTION = "\\caption"
    const val CAPTION_OF = "\\captionof"
    const val ITEM = "\\item"

    // 10. Import package

    // 11. Other common commands
    const val QED_HERE = "\\qedhere"
    const val URL = "\\url"
    const val HREF = "\\href"
    const val BIB_ITEM = "\\bibitem"

    // 12. Bracket commands
    const val L_BRACKET = "\\["
    const val R_BRACKET = "\\]"

    // 13. Language injection
    const val DIRECT_LUA = "\\directlua"
    const val LUA_EXEC = "\\luaexec"

    // 14. If-related commands (ignored in inspections)
    const val IFF = "\\iff"
    const val IF_THEN_ELSE = "\\ifthenelse"
    const val IF_TOGGLE = "\\iftoggle"
    const val IFOOT = "\\ifoot"
    const val IF_CSVSTRCMP = "\\ifcsvstrcmp"

    // Foldable footnotes
    const val FOOT_NOTE = "\\footnote"
    const val FOOT_CITE = "\\footcite"

    // to-do commands
    const val TODO = "\\todo"
    const val MISSING_FIGURE = "\\missingfigure"

    // 15. Glossaries/glossary-entries related
    const val NEW_ACRONYM = "\\newacronym"
    const val NEW_ABBREVIATION = "\\newabbreviation"
    const val NEW_GLOSSARY_ENTRY = "\\newglossaryentry"
    const val LONG_NEW_GLOSSARY_ENTRY = "\\longnewglossaryentry"
    const val ACRO = "\\acro"
    const val NEW_ACRO = "\\newacro"
    const val ACRO_DEF = "\\acrodef"
}