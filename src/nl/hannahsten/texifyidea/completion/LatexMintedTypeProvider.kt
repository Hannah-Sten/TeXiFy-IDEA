package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

/**
 * Provide autocompletion for minted languages in an \inputminted command. The harcoded list is only used as a backup.
 *
 * @author jojo2357
 */
object LatexMintedTypeProvider : LatexContextAgnosticCompletionProvider() {
    private val FALLBACK_LANGUAGES = setOf(
        LatexMintedLanguage("JSONBareObject", listOf(""), listOf()),
        LatexMintedLanguage("Raw token data", listOf(""), listOf()),
        LatexMintedLanguage("ABAP", listOf("abap"), listOf("*.abap", "*.ABAP")),
        LatexMintedLanguage("ActionScript", listOf("actionscript", "as"), listOf("*.as")),
        LatexMintedLanguage("Ada", listOf("ada", "ada95", "ada2005"), listOf("*.adb", "*.ads", "*.ada")),
        LatexMintedLanguage("ANTLR", listOf("antlr"), listOf()),
        LatexMintedLanguage("ApacheConf", listOf("apacheconf", "aconf", "apache"), listOf(".htaccess", "apache.conf", "apache2.conf")),
        LatexMintedLanguage("AppleScript", listOf("applescript"), listOf("*.applescript")),
        LatexMintedLanguage("AspectJ", listOf("aspectj"), listOf("*.aj")),
        LatexMintedLanguage("Asymptote", listOf("asymptote", "asy"), listOf("*.asy")),
        LatexMintedLanguage("autohotkey", listOf("autohotkey", "ahk"), listOf("*.ahk", "*.ahkl")),
        LatexMintedLanguage("AutoIt", listOf("autoit"), listOf("*.au3")),
        LatexMintedLanguage("Awk", listOf("awk", "gawk", "mawk", "nawk"), listOf("*.awk")),
        LatexMintedLanguage("Base Makefile", listOf("basemake"), listOf()),
        LatexMintedLanguage("Bash", listOf("bash", "sh", "ksh", "zsh", "shell"), listOf("*.sh", "*.ksh", "*.bash", "*.ebuild", "*.eclass", "*.exheres-0", "*.exlib", "*.zsh", ".bashrc", "bashrc", ".bash_*", "bash_*", "zshrc", ".zshrc", ".kshrc", "kshrc", "PKGBUILD")),
        LatexMintedLanguage("Batchfile", listOf("batch", "bat", "dosbatch", "winbatch"), listOf("*.bat", "*.cmd")),
        LatexMintedLanguage("BBCode", listOf("bbcode"), listOf()),
        LatexMintedLanguage("Befunge", listOf("befunge"), listOf("*.befunge")),
        LatexMintedLanguage("BlitzMax", listOf("blitzmax", "bmax"), listOf("*.bmx")),
        LatexMintedLanguage("Boo", listOf("boo"), listOf("*.boo")),
        LatexMintedLanguage("Brainfuck", listOf("brainfuck", "bf"), listOf("*.bf", "*.b")),
        LatexMintedLanguage("BUGS", listOf("bugs", "winbugs", "openbugs"), listOf("*.bug")),
        LatexMintedLanguage("C", listOf("c"), listOf("*.c", "*.h", "*.idc", "*.x[bp]m")),
        LatexMintedLanguage("Ceylon", listOf("ceylon"), listOf("*.ceylon")),
        LatexMintedLanguage("Coldfusion HTML", listOf("cfm"), listOf("*.cfm", "*.cfml")),
        LatexMintedLanguage("cfstatement", listOf("cfs"), listOf()),
        LatexMintedLanguage("Cheetah", listOf("cheetah", "spitfire"), listOf("*.tmpl", "*.spt")),
        LatexMintedLanguage("Clojure", listOf("clojure", "clj"), listOf("*.clj", "*.cljc")),
        LatexMintedLanguage("CMake", listOf("cmake"), listOf("*.cmake", "CMakeLists.txt")),
        LatexMintedLanguage("COBOL", listOf("cobol"), listOf("*.cob", "*.COB", "*.cpy", "*.CPY")),
        LatexMintedLanguage("Common Lisp", listOf("common-lisp", "cl", "lisp"), listOf("*.cl", "*.lisp")),
        LatexMintedLanguage("Bash Session", listOf("console", "shell-session"), listOf("*.sh-session", "*.shell-session")),
        LatexMintedLanguage("Coq", listOf("coq"), listOf("*.v")),
        LatexMintedLanguage("C++", listOf("cpp", "c++"), listOf("*.cpp", "*.hpp", "*.c++", "*.h++", "*.cc", "*.hh", "*.cxx", "*.hxx", "*.C", "*.H", "*.cp", "*.CPP", "*.tpp")),
        LatexMintedLanguage("Croc", listOf("croc"), listOf("*.croc")),
        LatexMintedLanguage("C#", listOf("csharp", "c#", "cs"), listOf("*.cs")),
        LatexMintedLanguage("CSS", listOf("css"), listOf("*.css")),
        LatexMintedLanguage("CUDA", listOf("cuda", "cu"), listOf("*.cu", "*.cuh")),
        LatexMintedLanguage("D", listOf("d"), listOf("*.d", "*.di")),
        LatexMintedLanguage("Debian Control file", listOf("debcontrol", "control"), listOf("control")),
        LatexMintedLanguage("dg", listOf("dg"), listOf("*.dg")),
        LatexMintedLanguage("Diff", listOf("diff", "udiff"), listOf("*.diff", "*.patch")),
        LatexMintedLanguage("Django/Jinja", listOf("django", "jinja"), listOf()),
        LatexMintedLanguage("Darcs Patch", listOf("dpatch"), listOf("*.dpatch", "*.darcspatch")),
        LatexMintedLanguage("Duel", listOf("duel", "jbst", "jsonml+bst"), listOf("*.duel", "*.jbst")),
        LatexMintedLanguage("Dylan", listOf("dylan"), listOf("*.dylan", "*.dyl", "*.intr")),
        LatexMintedLanguage("eC", listOf("ec"), listOf("*.ec", "*.eh")),
        LatexMintedLanguage("ERB", listOf("erb"), listOf()),
        LatexMintedLanguage("Evoque", listOf("evoque"), listOf("*.evoque")),
        LatexMintedLanguage("Fantom", listOf("fan"), listOf("*.fan")),
        LatexMintedLanguage("Fancy", listOf("fancy", "fy"), listOf("*.fy", "*.fancypack")),
        LatexMintedLanguage("Fortran", listOf("fortran", "f90"), listOf("*.f03", "*.f90", "*.F03", "*.F90")),
        LatexMintedLanguage("GAS", listOf("gas", "asm"), listOf("*.s", "*.S")),
        LatexMintedLanguage("Genshi", listOf("genshi", "kid", "xml+genshi", "xml+kid"), listOf("*.kid")),
        LatexMintedLanguage("Gherkin", listOf("gherkin", "cucumber"), listOf("*.feature")),
        LatexMintedLanguage("GLSL", listOf("glsl"), listOf("*.vert", "*.frag", "*.geo")),
        LatexMintedLanguage("Gnuplot", listOf("gnuplot"), listOf("*.plot", "*.plt")),
        LatexMintedLanguage("Go", listOf("go", "golang"), listOf("*.go")),
        LatexMintedLanguage("Gosu", listOf("gosu"), listOf("*.gs", "*.gsx", "*.gsp", "*.vark")),
        LatexMintedLanguage("Groovy", listOf("groovy"), listOf("*.groovy", "*.gradle")),
        LatexMintedLanguage("Gosu Template", listOf("gst"), listOf("*.gst")),
        LatexMintedLanguage("Haml", listOf("haml"), listOf("*.haml")),
        LatexMintedLanguage("Haskell", listOf("haskell", "hs"), listOf("*.hs")),
        LatexMintedLanguage("Haxe", listOf("haxe", "hxsl", "hx"), listOf("*.hx", "*.hxsl")),
        LatexMintedLanguage("Hxml", listOf("haxeml", "hxml"), listOf("*.hxml")),
        LatexMintedLanguage("HTML", listOf("html"), listOf("*.html", "*.htm", "*.xhtml", "*.xslt")),
        LatexMintedLanguage("HTTP", listOf("http"), listOf()),
        LatexMintedLanguage("IDL", listOf("idl"), listOf("*.pro")),
        LatexMintedLanguage("INI", listOf("ini", "cfg", "dosini"), listOf("*.ini", "*.cfg", "*.inf", ".editorconfig")),
        LatexMintedLanguage("IRC logs", listOf("irc"), listOf("*.weechatlog")),
        LatexMintedLanguage("Java", listOf("java"), listOf("*.java")),
        LatexMintedLanguage("JavaScript", listOf("javascript", "js"), listOf("*.js", "*.jsm", "*.mjs", "*.cjs")),
        LatexMintedLanguage("JSON", listOf("json", "json-object"), listOf("*.json", "*.jsonl", "*.ndjson", "Pipfile.lock")),
        LatexMintedLanguage("Java Server Page", listOf("jsp"), listOf("*.jsp")),
        LatexMintedLanguage("Kconfig", listOf("kconfig", "menuconfig", "linux-config", "kernel-config"), listOf("Kconfig*", "*Config.in*", "external.in*", "standard-modules.in")),
        LatexMintedLanguage("Koka", listOf("koka"), listOf("*.kk", "*.kki")),
        LatexMintedLanguage("Lasso", listOf("lasso", "lassoscript"), listOf("*.lasso", "*.lasso[89]")),
        LatexMintedLanguage("LLVM", listOf("llvm"), listOf("*.ll")),
        LatexMintedLanguage("Logos", listOf("logos"), listOf("*.x", "*.xi", "*.xm", "*.xmi")),
        LatexMintedLanguage("Lua", listOf("lua"), listOf("*.lua", "*.wlua")),
        LatexMintedLanguage("Mako", listOf("mako"), listOf("*.mao")),
        LatexMintedLanguage("Mako", listOf("mako"), listOf("*.mao")),
        LatexMintedLanguage("Mason", listOf("mason"), listOf("*.m", "*.mhtml", "*.mc", "*.mi", "autohandler", "dhandler")),
        LatexMintedLanguage("Matlab", listOf("matlab"), listOf("*.m")),
        LatexMintedLanguage("MiniD", listOf("minid"), listOf()),
        LatexMintedLanguage("Monkey", listOf("monkey"), listOf("*.monkey")),
        LatexMintedLanguage("MoonScript", listOf("moonscript", "moon"), listOf("*.moon")),
        LatexMintedLanguage("MXML", listOf("mxml"), listOf("*.mxml")),
        LatexMintedLanguage("Myghty", listOf("myghty"), listOf("*.myt", "autodelegate")),
        LatexMintedLanguage("MySQL", listOf("mysql"), listOf()),
        LatexMintedLanguage("NASM", listOf("nasm"), listOf("*.asm", "*.ASM", "*.nasm")),
        LatexMintedLanguage("NewLisp", listOf("newlisp"), listOf("*.lsp", "*.nl", "*.kif")),
        LatexMintedLanguage("Newspeak", listOf("newspeak"), listOf("*.ns2")),
        LatexMintedLanguage("NumPy", listOf("numpy"), listOf()),
        LatexMintedLanguage("OCaml", listOf("ocaml"), listOf("*.ml", "*.mli", "*.mll", "*.mly")),
        LatexMintedLanguage("Octave", listOf("octave"), listOf("*.m")),
        LatexMintedLanguage("Ooc", listOf("ooc"), listOf("*.ooc")),
        LatexMintedLanguage("Perl", listOf("perl", "pl"), listOf("*.pl", "*.pm", "*.t", "*.perl")),
        LatexMintedLanguage("PHP", listOf("php", "php3", "php4", "php5"), listOf("*.php", "*.php[345]", "*.inc")),
        LatexMintedLanguage("PL/pgSQL", listOf("plpgsql"), listOf()),
        LatexMintedLanguage("PostgreSQL SQL dialect", listOf("postgresql", "postgres"), listOf()),
        LatexMintedLanguage("PostScript", listOf("postscript", "postscr"), listOf("*.ps", "*.eps")),
        LatexMintedLanguage("Gettext Catalog", listOf("pot", "po"), listOf("*.pot", "*.po")),
        LatexMintedLanguage("Prolog", listOf("prolog"), listOf("*.ecl", "*.prolog", "*.pro", "*.pl")),
        LatexMintedLanguage("PostgreSQL console (psql", listOf("psql", "postgresql-console", "postgres-console"), listOf()),
        LatexMintedLanguage("Pug", listOf("pug", "jade"), listOf("*.pug", "*.jade")),
        LatexMintedLanguage("Puppet", listOf("puppet"), listOf("*.pp")),
        LatexMintedLanguage("Python", listOf("python", "py", "sage", "python3", "py3", "bazel", "starlark"), listOf("*.py", "*.pyw", "*.pyi", "*.jy", "*.sage", "*.sc", "SConstruct", "SConscript", "*.bzl", "BUCK", "BUILD", "BUILD.bazel", "WORKSPACE", "*.tac")),
        LatexMintedLanguage("QML", listOf("qml", "qbs"), listOf("*.qml", "*.qbs")),
        LatexMintedLanguage("Ragel", listOf("ragel"), listOf()),
        LatexMintedLanguage("RHTML", listOf("rhtml", "html+erb", "html+ruby"), listOf("*.rhtml")),
        LatexMintedLanguage("Ruby", listOf("ruby", "rb", "duby"), listOf("*.rb", "*.rbw", "Rakefile", "*.rake", "*.gemspec", "*.rbx", "*.duby", "Gemfile", "Vagrantfile")),
        LatexMintedLanguage("Sass", listOf("sass"), listOf("*.sass")),
        LatexMintedLanguage("Scheme", listOf("scheme", "scm"), listOf("*.scm", "*.ss")),
        LatexMintedLanguage("Smalltalk", listOf("smalltalk", "squeak", "st"), listOf("*.st")),
        LatexMintedLanguage("SQL", listOf("sql"), listOf("*.sql")),
        LatexMintedLanguage("Scalate Server Page", listOf("ssp"), listOf("*.ssp")),
        LatexMintedLanguage("Tcl", listOf("tcl"), listOf("*.tcl", "*.rvt")),
        LatexMintedLanguage("Tea", listOf("tea"), listOf("*.tea")),
        LatexMintedLanguage("TeX", listOf("tex", "latex"), listOf("*.tex", "*.aux", "*.toc")),
        LatexMintedLanguage("Text only", listOf("text"), listOf("*.txt")),
        LatexMintedLanguage("Vala", listOf("vala", "vapi"), listOf("*.vala", "*.vapi")),
        LatexMintedLanguage("VGL", listOf("vgl"), listOf("*.rpf")),
        LatexMintedLanguage("VimL", listOf("vim"), listOf("*.vim", ".vimrc", ".exrc", ".gvimrc", "_vimrc", "_exrc", "_gvimrc", "vimrc", "gvimrc")),
        LatexMintedLanguage("XML", listOf("xml"), listOf("*.xml", "*.xsl", "*.rss", "*.xslt", "*.xsd", "*.wsdl", "*.wsf")),
        LatexMintedLanguage("XQuery", listOf("xquery", "xqy", "xq", "xql", "xqm"), listOf("*.xqy", "*.xquery", "*.xq", "*.xql", "*.xqm")),
        LatexMintedLanguage("YAML", listOf("yaml"), listOf("*.yaml", "*.yml")),
        LatexMintedLanguage("Zeek", listOf("zeek", "bro"), listOf("*.zeek", "*.bro")),
    )

    private val LANGUAGES: Set<LatexMintedLanguage> by lazy {
        val back = "pygmentize -L lexers --json".runCommandWithExitCode()

        val retJason = back.first ?: return@lazy FALLBACK_LANGUAGES

        if (back.second != 0) {
            FALLBACK_LANGUAGES
        }
        else {
            Json.parseToJsonElement(retJason).jsonObject["lexers"]!!.jsonObject.entries.map { (key, value) -> LatexMintedLanguage(key, value.jsonObject["aliases"]!!.jsonArray.map { it.jsonPrimitive.content }, value.jsonObject["filenames"]!!.jsonArray.map { it.jsonPrimitive.content }) }.toSet()
        }
    }

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        result.addAllElements(
            LANGUAGES.flatMap { lang -> lang.createLookupElement() }
        )
    }
}

class LatexMintedLanguage(
    val languageName: String,
    val languageAbbr: List<String>,
    val fileExtensions: List<String>,
) {
    fun createLookupElement(): List<LookupElement> = languageAbbr.map {
        LookupElementBuilder.create(it)
            .withPresentableText(languageName)
            .bold()
            .withLookupStrings(listOf(languageName))
            .withCaseSensitivity(false)
            .withTypeText(it)
    }
}