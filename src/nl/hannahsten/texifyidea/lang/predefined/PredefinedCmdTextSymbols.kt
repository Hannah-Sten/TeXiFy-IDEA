@file:Suppress("unused")

package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet

object PredefinedCmdTextSymbols : PredefinedCommandSet() {

    val escapedSymbols = buildCommands {
        symbol("$", "$", "Dollar sign")
        symbol("%", "%", "Percent sign")
        symbol("&", "&", "Ampersand")
        symbol("#", "#", "Hash sign")
        symbol("{", "{", "Opening brace")
        symbol("}", "}", "Closing brace")
        symbol("_", "_", "Underscore")
    }

    val textcomp = textCommands {
        packageOf("textcomp")

        symbol("textacutedbl", "̋")
        symbol("textasciiacute", "´")
        symbol("textasciibreve", "˘")
        symbol("textasciicaron", "˘")
        symbol("textasciidieresis", "¨")
        symbol("textasciigrave", "`")
        symbol("textasciimacron", "¯")
        symbol("textasteriskcentered", "∗")
        symbol("textbaht", "฿")
        symbol("textbardbl", "‖")
        symbol("textbigcircle", "○")
        symbol("textblank", "␢")
        symbol("textborn", "★")
        symbol("textbrokenbar", "¦")
        symbol("textcelsius", "℃")
        symbol("textcent", "¢")
        symbol("textcentoldstyle", null)
        symbol("textcolonmonetary", "₡")
        symbol("textcopyleft", "🄯")
        symbol("textcurrency", "¤")
        symbol("textdblhyphen", null)
        symbol("textdblhyphenchar", null)
        symbol("textdegree", "°")
        symbol("textdied", "✝")
        symbol("textdiscount", "⁒")
        symbol("textdiv", "÷")
        symbol("textdivorced", "⚮")
        symbol("textdollaroldstyle", null)
        symbol("textdong", "₫")
        symbol("textestimated", "℮")
        symbol("texteuro", "€")
        symbol("textflorin", "ƒ")
        symbol("textfractionsolidus", "⁄")
        symbol("textgravedbl", "̏")
        symbol("textguarani", "₲")
        symbol("textinterrobang", "‽")
        symbol("textinterrobangdown", null)
        symbol("textlangle", "〈")
        symbol("textlbrackdbl", "〚")
        symbol("textleaf", null)
        symbol("textlira", "₤")
        symbol("textlnot", "¬")
        symbol("textlquill", "⁅")
        symbol("textmarried", "⚭")
        symbol("textmho", "℧")
        symbol("textminus", "−")
        symbol("textmu", "μ")
        symbol("textmusicalnote", "♪")
        symbol("textnaira", "₦")
        symbol("textnumero", "№")
        symbol("textohm", "Ω")
        symbol("textonehalf", "½")
        symbol("textonequarter", "¼")
        symbol("textonesuperior", "¹")
        symbol("textopenbullet", "◦")
        symbol("textordfeminine", "ª")
        symbol("textordmasculine", "º")
        symbol("textpertenthousand", "‱")
        symbol("textperthousand", "‰")
        symbol("textpeso", "₱")
        symbol("textpilcrow", "¶")
        symbol("textpm", "±")
        symbol("textquotesingle", "‛")
        symbol("textquotestraightbase", "‚")
        symbol("textquotestraightdblbase", "„")
        symbol("textrangle", "〉")
        symbol("textrbrackdbl", "〛")
        symbol("textrecipe", "℞")
        symbol("textreferencemark", "※")
        symbol("textrquill", "⁆")
        symbol("textservicemark", "℠")
        symbol("textsurd", "√")
        symbol("textthreequarters", "¾")
        symbol("textthreequartersemdash", "—")
        symbol("textthreesuperior", "³")
        symbol("texttildelow", "˷")
        symbol("texttimes", "×")
        symbol("texttwelveudash", "—")
        symbol("texttwosuperior", "²")
        symbol("textwon", "₩")
        symbol("textyen", "¥")

        symbol("textdownarrow", "↓")
        symbol("textleftarrow", "←")
        symbol("textrightarrow", "→")
        symbol("textuparrow", "↑")
        symbol("texttrademark", "™")
        symbol("textunderscore", "_")
        symbol("textvisiblespace", "␣")
    }
    val euro = buildCommands {

        packageOf("eurosym")
        "EUR".cmd("amount".required) { "EURO_AMOUNT" }
        symbol("euro", "€")
        symbol("geneuro", "€")
        symbol("geneuronarrow", "€")
        symbol("geneurowide", "€")
        symbol("officialeuro", "€")
    }

    val generalSymbols = buildCommands {

        underContext(LatexContexts.Text) {
            // while they can be used in math mode, they are not usually used there
            symbol("OE", "Œ")
            symbol("S", "§")
            symbol("aa", "å")
            symbol("ae", "æ")
        }

        symbol("dag", "†")
        symbol("ddag", "‡")

        symbol("i", "ı")
        symbol("lbrack", "[")
        symbol("lq", "‘")
        symbol("oe", "œ")
        symbol("pounds", "£")
        symbol("rbrack", "]")
        symbol("rq", "’")

        packageOf("babel")
        symbol("flq", "‹")
        symbol("flqq", "«")
        symbol("frq", "›")
        symbol("frqq", "»")

        symbol("glq", ",")
        symbol("glqq", "„")
        symbol("grq", "‘")
        symbol("grqq", "“")

        packageOf("marvosym")
        symbol("Cutleft", null)
        symbol("Cutline", null)
        symbol("Cutright", "✁")
        symbol("Kutline", null)
        symbol("Leftscissors", null)
        symbol("Rightscissors", "✂")

        packageOf("amssymb")
        symbol("checkmark", "✓")
        symbol("maltese", "✠")

        packageOf("wasysym")
        symbol("AC", "∼")
        symbol("APLbox", "⎕")
        symbol("APLcirc{}", null)
        symbol("APLcomment", null)
        symbol("APLdown", "⍗")
        symbol("APLdownarrowbox", null)
        symbol("APLinput", "⍞")
        symbol("APLinv", "⌹")
        symbol("APLleftarrowbox", "⍇")
        symbol("APLlog", "⍟")
        symbol("APLminus", "—")
        symbol("APLnot{}", "∼")
        symbol("APLrightarrowbox", "⍈")
        symbol("APLstar", "🞯")
        symbol("APLup", null)
        symbol("APLuparrowbox", "⍐")
        symbol("APLvert{}", "|")
        symbol("Bowtie", "⋈")
        symbol("CIRCLE", "⏺")
        symbol("CheckedBox", "☑")
        symbol("Circle", "○")
        symbol("DOWNarrow", "▼")
        symbol("HF", "∼")
        symbol("LEFTCIRCLE", "◖")
        symbol("LEFTarrow", "⯇")
        symbol("LEFTcircle", "◖")
        symbol("Leftcircle", null)
        symbol("RIGHTCIRCLE", "◗")
        symbol("RIGHTarrow", "⯈")
        symbol("RIGHTcircle", "◗")
        symbol("Rightcircle", null)
        symbol("Square", "□")
        symbol("Thorn", "þ")
        symbol("UParrow", "▲")
        symbol("VHF", "≋")
        symbol("XBox", "☒")
        symbol("agemO", "℧")
        symbol("applecmd", "⌘")
        symbol("aquarius", "♒")
        symbol("aries", "♈")
        symbol("ascnode", "☊")
        symbol("astrosun", "☉")
        symbol("ataribox", null)
        symbol("bell", "🔔")
        symbol("blacksmiley", "☻")
        symbol("brokenvert", "¦")
        symbol("cancer", "♋")
        symbol("capricornus", "♑")
        symbol("cent", "¢")
        symbol("checked", "✓")
        symbol("clock", "⏲")
        symbol("conjunction", "☌")
        symbol("currency", "¤")
        symbol("davidsstar", "✡")
        symbol("descnode", "☋")
        symbol("diameter", "⌀")
        symbol("earth", "♁")
        symbol("eighthnote", "♪")
        symbol("female", "♀")
        symbol("frownie", "☹")
        symbol("fullmoon", "○")
        symbol("fullnote", "𝅝")
        symbol("gemini", "♊")
        symbol("gluon", null)
        symbol("halfnote", "𝅗𝅥")
        symbol("hexagon", null)
        symbol("hexstar", null)
        symbol("invdiameter", null)
        symbol("inve", "Ə")
        symbol("jupiter", "♃")
        symbol("kreuz", "✠")
        symbol("leftmoon", "☾")
        symbol("leftturn", "↺")
        symbol("leo", "♌")
        symbol("libra", "♎")
        symbol("lightning", "☇")
        symbol("male", "♂")
        symbol("mars", "♂")
        symbol("mercury", "☿")
        symbol("neptune", "♆")
        symbol("newmoon", "◯")
        symbol("notbackslash", null)
        symbol("notslash", null)
        symbol("octagon", null)
        symbol("openo", "ᵓ")
        symbol("opposition", "☍")
        symbol("pentagon", "⬠")
        symbol("permil", "‰")
        symbol("phone", "☏")
        symbol("photon", "〜〜〜")
        symbol("pisces", "♓")
        symbol("pluto", "♇")
        symbol("pointer", "⇨")
        symbol("quarternote", "")
        symbol("recorder", "♩")
        symbol("rightmoon", "☽")
        symbol("rightturn", "↻")
        symbol("sagittarius", "♐")
        symbol("saturn", "♄")
        symbol("scorpio", "♏")
        symbol("smiley", "☺")
        symbol("sun", "☼")
        symbol("taurus", "♉")
        symbol("thorn", "þ")
        symbol("twonotes", "♫")
        symbol("uranus", "⛢")
        symbol("varangle", "∡")
        symbol("varhexagon", "⬡")
        symbol("varhexstar", "🞵")
        symbol("venus", "♀")
        symbol("vernal", "♈")
        symbol("virgo", "♍")
        symbol("wasycmd", "⌘")
        symbol("wasylozenge", "⯏")
        symbol("wasytherefore", "∴")
    }

    val textSymbols = textCommands {

        symbol("textasciicircum", "^")
        symbol("textasciitilde", "~")
        symbol("textasteriskcentered", "⁎")
        symbol("textbackslash", "\\")
        symbol("textbar", "|")
        symbol("textbraceleft", "{")
        symbol("textbraceright", "}")
        symbol("textbullet", "•")
        symbol("textcopyright", "©")
        symbol("textdagger", "†")
        symbol("textdaggerdbl", "‡")
        symbol("textdollar", "$")
        symbol("textellipsis", "…")
        symbol("textemdash", "—")
        symbol("textendash", "–")
        symbol("textexclamdown", "¡")
        symbol("textgreater", ">")
        symbol("textless", "<")
        symbol("textperiodcentered", "·")
        symbol("textquestiondown", "¿")
        symbol("textquotedblleft", "“")
        symbol("textquotedblright", "”")
        symbol("textquoteleft", "‘")
        symbol("textquoteright", "’")
        symbol("textregistered", "®")
        symbol("textsection", "§")
        symbol("textsterling", "£")
    }

    val siunitx = buildCommands {
        packageOf("siunitx")
        "ang".cmd("options".optional, "angle".required) { "ANG" }
        "complexnum".cmd("options".optional, "number".required) { "COMPLEXNUM" }
        "complexqty".cmd("options".optional, "number".required, "unit".required) { "COMPLEXQTY" }
        symbol("micro", "µ")
        "num".cmd("options".optional, "number".required) { "NUM" }
        "numlist".cmd("options".optional, "numbers".required) { "NUMLIST" }
        "numproduct".cmd("options".optional, "numbers".required) { "NUMPRODUCT" }
        "numrange".cmd("options".optional, "number1".required, "number2".required) { "NUMRANGE" }
        symbol("ohm", "Ω")
        "qty".cmd("options".optional, "number".required, "unit".required) { "QTY" }
        "qtylist".cmd("options".optional, "numbers".required, "unit".required) { "QTYLIST" }
        "qtyproduct".cmd("options".optional, "numbers".required, "unit".required) { "QTYPRODUCT" }
        "qtyrange".cmd("options".optional, "number1".required, "number2".required, "unit".required) { "QTYRANGE" }
        "sisetup".cmd("options".required) { "SISETUP" }
        "tablenum".cmd("options".optional, "number".required) { "TABLENUM" }
        "unit".cmd("options".optional, "unit".required) { "UNIT" }
    }

    val loremIpsum = buildCommands {
        val list = "list".required(LatexContexts.ListType)
        +"Blinddocument"
        "Blindlist".cmd(list) { "LONG_BLIND_LIST" }
        "Blindlistoptional".cmd(list) { "LONG_BLIND_LIST_OPTIONAL" }
        "Blindtext".cmd("paragraphs".optional, "repetitions".optional) { "LONG_BLIND_TEXT" }
        +"blinddescription"
        +"blinddocument"
        +"blindenumerate"
        +"blinditemize"
        "blindlist".cmd(list) { "BLIND_LIST" }
        "blindlistlist".cmd("level".optional, list) { "BLIND_LIST_LIST" }
        "blindlistoptional".cmd(list) { "BLIND_LIST_OPTIONAL" }
        "blindtext".cmd("repetitions".optional) { "BLIND_TEXT" }

        packageOf("lipsum")
        "lipsum".cmd("paragraph range".optional, "sentence range".optional) { "LIPSUM" }
        "lipsum*".cmd("paragraph range".optional, "sentence range".optional) { "LIPSUM_AS_SINGLE_PARAGRAPH" }
    }
}