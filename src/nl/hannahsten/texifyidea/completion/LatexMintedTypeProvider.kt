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
        LatexMintedLanguage("JSONBareObject", listOf("")),
        LatexMintedLanguage("Raw token data", listOf("")),
        LatexMintedLanguage("ABAP", listOf("abap")),
        LatexMintedLanguage("ActionScript", listOf("actionscript", "as")),
        LatexMintedLanguage("Ada", listOf("ada", "ada95", "ada2005")),
        LatexMintedLanguage("ANTLR", listOf("antlr")),
        LatexMintedLanguage("ApacheConf", listOf("apacheconf", "aconf", "apache")),
        LatexMintedLanguage("AppleScript", listOf("applescript")),
        LatexMintedLanguage("AspectJ", listOf("aspectj")),
        LatexMintedLanguage("Asymptote", listOf("asymptote", "asy")),
        LatexMintedLanguage("autohotkey", listOf("autohotkey", "ahk")),
        LatexMintedLanguage("AutoIt", listOf("autoit")),
        LatexMintedLanguage("Awk", listOf("awk", "gawk", "mawk", "nawk")),
        LatexMintedLanguage("Base Makefile", listOf("basemake")),
        LatexMintedLanguage("Bash", listOf("bash", "sh", "ksh", "zsh", "shell")),
        LatexMintedLanguage("Batchfile", listOf("batch", "bat", "dosbatch", "winbatch")),
        LatexMintedLanguage("BBCode", listOf("bbcode")),
        LatexMintedLanguage("Befunge", listOf("befunge")),
        LatexMintedLanguage("BlitzMax", listOf("blitzmax", "bmax")),
        LatexMintedLanguage("Boo", listOf("boo")),
        LatexMintedLanguage("Brainfuck", listOf("brainfuck", "bf")),
        LatexMintedLanguage("BUGS", listOf("bugs", "winbugs", "openbugs")),
        LatexMintedLanguage("C", listOf("c")),
        LatexMintedLanguage("Ceylon", listOf("ceylon")),
        LatexMintedLanguage("Coldfusion HTML", listOf("cfm")),
        LatexMintedLanguage("cfstatement", listOf("cfs")),
        LatexMintedLanguage("Cheetah", listOf("cheetah", "spitfire")),
        LatexMintedLanguage("Clojure", listOf("clojure", "clj")),
        LatexMintedLanguage("CMake", listOf("cmake")),
        LatexMintedLanguage("COBOL", listOf("cobol")),
        LatexMintedLanguage("Common Lisp", listOf("common-lisp", "cl", "lisp")),
        LatexMintedLanguage("Bash Session", listOf("console", "shell-session")),
        LatexMintedLanguage("Coq", listOf("coq")),
        LatexMintedLanguage("C++", listOf("cpp", "c++")),
        LatexMintedLanguage("Croc", listOf("croc")),
        LatexMintedLanguage("C#", listOf("csharp", "c#", "cs")),
        LatexMintedLanguage("CSS", listOf("css")),
        LatexMintedLanguage("CUDA", listOf("cuda", "cu")),
        LatexMintedLanguage("D", listOf("d")),
        LatexMintedLanguage("Debian Control file", listOf("debcontrol", "control")),
        LatexMintedLanguage("dg", listOf("dg")),
        LatexMintedLanguage("Diff", listOf("diff", "udiff")),
        LatexMintedLanguage("Django/Jinja", listOf("django", "jinja")),
        LatexMintedLanguage("Darcs Patch", listOf("dpatch")),
        LatexMintedLanguage("Duel", listOf("duel", "jbst", "jsonml+bst")),
        LatexMintedLanguage("Dylan", listOf("dylan")),
        LatexMintedLanguage("eC", listOf("ec")),
        LatexMintedLanguage("ERB", listOf("erb")),
        LatexMintedLanguage("Evoque", listOf("evoque")),
        LatexMintedLanguage("Fantom", listOf("fan")),
        LatexMintedLanguage("Fancy", listOf("fancy", "fy")),
        LatexMintedLanguage("Fortran", listOf("fortran", "f90")),
        LatexMintedLanguage("GAS", listOf("gas", "asm")),
        LatexMintedLanguage("Genshi", listOf("genshi", "kid", "xml+genshi", "xml+kid")),
        LatexMintedLanguage("Gherkin", listOf("gherkin", "cucumber")),
        LatexMintedLanguage("GLSL", listOf("glsl")),
        LatexMintedLanguage("Gnuplot", listOf("gnuplot")),
        LatexMintedLanguage("Go", listOf("go", "golang")),
        LatexMintedLanguage("Gosu", listOf("gosu")),
        LatexMintedLanguage("Groovy", listOf("groovy")),
        LatexMintedLanguage("Gosu Template", listOf("gst")),
        LatexMintedLanguage("Haml", listOf("haml")),
        LatexMintedLanguage("Haskell", listOf("haskell", "hs")),
        LatexMintedLanguage("Haxe", listOf("haxe", "hxsl", "hx")),
        LatexMintedLanguage("Hxml", listOf("haxeml", "hxml")),
        LatexMintedLanguage("HTML", listOf("html")),
        LatexMintedLanguage("HTTP", listOf("http")),
        LatexMintedLanguage("IDL", listOf("idl")),
        LatexMintedLanguage("INI", listOf("ini", "cfg", "dosini")),
        LatexMintedLanguage("IRC logs", listOf("irc")),
        LatexMintedLanguage("Java", listOf("java")),
        LatexMintedLanguage("JavaScript", listOf("javascript", "js")),
        LatexMintedLanguage("JSON", listOf("json", "json-object")),
        LatexMintedLanguage("Java Server Page", listOf("jsp")),
        LatexMintedLanguage("Kconfig", listOf("kconfig", "menuconfig", "linux-config", "kernel-config")),
        LatexMintedLanguage("Koka", listOf("koka")),
        LatexMintedLanguage("Lasso", listOf("lasso", "lassoscript")),
        LatexMintedLanguage("LLVM", listOf("llvm")),
        LatexMintedLanguage("Logos", listOf("logos")),
        LatexMintedLanguage("Lua", listOf("lua")),
        LatexMintedLanguage("Mako", listOf("mako")),
        LatexMintedLanguage("Mako", listOf("mako")),
        LatexMintedLanguage("Mason", listOf("mason")),
        LatexMintedLanguage("Matlab", listOf("matlab")),
        LatexMintedLanguage("MiniD", listOf("minid")),
        LatexMintedLanguage("Monkey", listOf("monkey")),
        LatexMintedLanguage("MoonScript", listOf("moonscript", "moon")),
        LatexMintedLanguage("MXML", listOf("mxml")),
        LatexMintedLanguage("Myghty", listOf("myghty")),
        LatexMintedLanguage("MySQL", listOf("mysql")),
        LatexMintedLanguage("NASM", listOf("nasm")),
        LatexMintedLanguage("NewLisp", listOf("newlisp")),
        LatexMintedLanguage("Newspeak", listOf("newspeak")),
        LatexMintedLanguage("NumPy", listOf("numpy")),
        LatexMintedLanguage("OCaml", listOf("ocaml")),
        LatexMintedLanguage("Octave", listOf("octave")),
        LatexMintedLanguage("Ooc", listOf("ooc")),
        LatexMintedLanguage("Perl", listOf("perl", "pl")),
        LatexMintedLanguage("PHP", listOf("php", "php3", "php4", "php5")),
        LatexMintedLanguage("PL/pgSQL", listOf("plpgsql")),
        LatexMintedLanguage("PostgreSQL SQL dialect", listOf("postgresql", "postgres")),
        LatexMintedLanguage("PostScript", listOf("postscript", "postscr")),
        LatexMintedLanguage("Gettext Catalog", listOf("pot", "po")),
        LatexMintedLanguage("Prolog", listOf("prolog")),
        LatexMintedLanguage("PostgreSQL console (psql", listOf("psql", "postgresql-console", "postgres-console")),
        LatexMintedLanguage("Pug", listOf("pug", "jade")),
        LatexMintedLanguage("Puppet", listOf("puppet")),
        LatexMintedLanguage("Python", listOf("python", "py", "sage", "python3", "py3", "bazel", "starlark")),
        LatexMintedLanguage("QML", listOf("qml", "qbs")),
        LatexMintedLanguage("Ragel", listOf("ragel")),
        LatexMintedLanguage("RHTML", listOf("rhtml", "html+erb", "html+ruby")),
        LatexMintedLanguage("Ruby", listOf("ruby", "rb", "duby")),
        LatexMintedLanguage("Sass", listOf("sass")),
        LatexMintedLanguage("Scheme", listOf("scheme", "scm")),
        LatexMintedLanguage("Smalltalk", listOf("smalltalk", "squeak", "st")),
        LatexMintedLanguage("SQL", listOf("sql")),
        LatexMintedLanguage("Scalate Server Page", listOf("ssp")),
        LatexMintedLanguage("Tcl", listOf("tcl")),
        LatexMintedLanguage("Tea", listOf("tea")),
        LatexMintedLanguage("TeX", listOf("tex", "latex")),
        LatexMintedLanguage("Text only", listOf("text")),
        LatexMintedLanguage("Vala", listOf("vala", "vapi")),
        LatexMintedLanguage("VGL", listOf("vgl")),
        LatexMintedLanguage("VimL", listOf("vim")),
        LatexMintedLanguage("XML", listOf("xml")),
        LatexMintedLanguage("XQuery", listOf("xquery", "xqy", "xq", "xql", "xqm")),
        LatexMintedLanguage("YAML", listOf("yaml")),
        LatexMintedLanguage("Zeek", listOf("zeek", "bro")),
    )

    private val LANGUAGES: Set<LatexMintedLanguage> by lazy {
        val back = "pygmentize -L lexers --json".runCommandWithExitCode()

        val retJason = back.first ?: return@lazy FALLBACK_LANGUAGES

        if (back.second != 0) {
            FALLBACK_LANGUAGES
        }
        else {
            Json.parseToJsonElement(retJason).jsonObject["lexers"]!!.jsonObject.entries.map { (key, value) -> LatexMintedLanguage(key, value.jsonObject["aliases"]!!.jsonArray.map { it.jsonPrimitive.content }) }.toSet()
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