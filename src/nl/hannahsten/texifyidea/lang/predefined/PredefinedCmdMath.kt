@file:Suppress("unused")

package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.editor.folding.MathStyle
import nl.hannahsten.texifyidea.lang.DSLLatexBuilderScope
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet

object PredefinedCmdMath : PredefinedCommandSet() {

    private inline fun DSLLatexBuilderScope.styleCmd(
        name: String, arg: LArgument, style: MathStyle,
        description: () -> String
    ) = command(name, listOf(arg), description(), display = null).apply {
        putMeta(MathStyle.META_KEY, style)
    }

    val textMathCommands = buildCommands {
        packageOf("amsmath")
        underPackage("amstext") {
            underContext(LatexContexts.Math) {
                "text".cmd("text".required(LatexContexts.Text)) { "Text in math" }
            }
        }

        underContexts(LatexContexts.Math, LatexContexts.Text, LatexContexts.InsideDefinition) {
            "ensuremath".cmd("math".required(LatexContexts.Math)) { "Ensure math mode" }
        }
    }

    val defaultMathArgCommands = mathCommands {

        "acute".cmd("a".required) { "Acute accent" }
        "bar".cmd("a".required) { "Bar accent" }
        "breve".cmd("a".required) { "Breve accent" }
        "check".cmd("a".required) { "Check accent" }
        "ddot".cmd("a".required) { "Double dot accent" }
        "dot".cmd("a".required) { "Dot accent" }
        "frac".cmd("num".required, "den".required) { "Fraction" }
        "grave".cmd("a".required) { "Grave accent" }
        "hat".cmd("a".required) { "Hat accent" }
        "mathring".cmd("a".required) { "Ring accent" }
        "overbrace".cmd("text".required) { "Overbrace" }
        "overleftarrow".cmd("text".required) { "Over left arrow" }
        "overrightarrow".cmd("text".required) { "Over right arrow" }
        "overline".cmd("text".required) { "Overline" }
        "sqrt".cmd("root".optional, "arg".required) { "Square root" }
        "stackrel".cmd("top".required, "relation".required) { "Stacked relation" }
        "tilde".cmd("a".required) { "Tilde accent" }
        "underbrace".cmd("text".required) { "Underbrace" }
        "vec".cmd("a".required) { "Vector accent" }
        "widehat".cmd("text".required) { "Wide hat" }
        "widetilde".cmd("text".required) { "Wide tilde" }

        packageOf("amsmath")
        "binom".cmd("total".required, "sample".required) { "Binomial coefficient" }
        "ddddot".cmd("a".required) { "Quadruple dots accent" }
        "dddot".cmd("a".required) { "Triple dots accent" }
        "dfrac".cmd("num".required, "den".required) { "Display fraction" }
        "overleftrightarrow".cmd("text".required) { "Over left right arrow" }
        "underleftarrow".cmd("text".required) { "Under left arrow" }
        "underleftrightarrow".cmd("text".required) { "Under left right arrow" }
        "underrightarrow".cmd("text".required) { "Under right arrow" }
        "xleftarrow".cmd("text".required) { "Above left arrow" }
        "xrightarrow".cmd("text".required) { "Above right arrow" }
    }

    // Not applicable for symbols tool window
    val otherMathArgCommands = mathCommands {
        packageOf("amsmath")
        "intertext".cmd("text".required) { "Intertext" }
        "mathgroup".cmd()
    }

    val fontCommands = mathCommands {
        val arg = "text".required
        styleCmd("mathbf", arg, MathStyle.BOLD) { "Bold" }
        styleCmd("mathcal", arg, MathStyle.CALLIGRAPHIC) { "Calligraphic" }

        styleCmd("mathit", arg, MathStyle.ITALIC) { "Italic" }
        "mathnormal".cmd(arg) { "Normal" }
        styleCmd("mathsf", arg, MathStyle.SANS_SERIF) { "Sans-serif" }
        styleCmd("mathrm", arg, MathStyle.ROMAN) { "Roman" }
        styleCmd("mathscr", arg, MathStyle.SCRIPT) { "Script" }
        styleCmd("mathtt", arg, MathStyle.MONOSPACE) { "Typewriter" }

        packageOf("amsmath")
        styleCmd("boldsymbol", arg, MathStyle.BOLD) { "Bold symbols" }

        packageOf("amsfonts")
        styleCmd("mathbb", arg, MathStyle.BLACKBOARD_BOLD) { "Blackboard bold" }
        styleCmd("mathfrak", arg, MathStyle.FRAKTUR) { "Fraktur" }

        packageOf("bbm")
        styleCmd("mathbbm", arg, MathStyle.BLACKBOARD_BOLD) { "Blackboard bold (bbm)" }
        styleCmd("mathbbmss", arg, MathStyle.BLACKBOARD_BOLD) { "Blackboard bold sans-serif" }
        styleCmd("mathbbmtt", arg, MathStyle.BLACKBOARD_BOLD) { "Blackboard bold typewriter" }

        packageOf("bm")
        styleCmd("bm", arg, MathStyle.BOLD) { "Bold math" }

        packageOf("dsfont")
        "mathds".cmd(arg) { "Double-struck" }

        underPackage("unicode-math") {
            styleCmd("mathup", arg, MathStyle.ROMAN) { "Upright mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathbfup", arg, MathStyle.BOLD) { "Bold upright mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathbfit", arg, MathStyle.BOLD_ITALIC) { "Bold italic mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathsfup", arg, MathStyle.SANS_SERIF) { "Sans-serif upright mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathsfit", arg, MathStyle.SANS_SERIF) { "Sans-serif italic mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathbfsfup", arg, MathStyle.BOLD_SANS_SERIF_UPRIGHT) { "Bold upright sans-serif mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathbfsfit", arg, MathStyle.BOLD_SANS_SERIF_ITALIC) { "Bold italic sans-serif mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathbfsf", arg, MathStyle.BOLD_SANS_SERIF_UPRIGHT) { "Bold sans-serif mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathbfcal", arg, MathStyle.BOLD_SCRIPT) { "Bold calligraphic mathematical alphabet (unicode-math compatibility)." }
            styleCmd("mathbbit", arg, MathStyle.BLACKBOARD_BOLD) { "Blackboard bold italic mathematical alphabet (unicode-math compatibility)." }
        }
    }

    val unicodeMathStyleCommands = mathCommands {
        val arg = "text".required

        underPackage("unicode-math") {
            styleCmd("symup", arg, MathStyle.ROMAN) { "Upright mathematical alphabet (unicode-math)." }
            styleCmd("symrm", arg, MathStyle.ROMAN) { "Roman upright mathematical alphabet (unicode-math)." }
            styleCmd("symit", arg, MathStyle.ITALIC) { "Italic mathematical alphabet (unicode-math)." }
            styleCmd("symbf", arg, MathStyle.BOLD) { "Bold mathematical alphabet (unicode-math)." }
            styleCmd("symbfup", arg, MathStyle.BOLD) { "Bold upright mathematical alphabet (unicode-math)." }
            styleCmd("symcal", arg, MathStyle.CALLIGRAPHIC) { "Calligraphic mathematical alphabet (unicode-math)." }
            styleCmd("symbfcal", arg, MathStyle.BOLD_SCRIPT) { "Bold calligraphic mathematical alphabet (unicode-math)." }
            styleCmd("symbb", arg, MathStyle.BLACKBOARD_BOLD) { "Blackboard bold mathematical alphabet (unicode-math)." }
            styleCmd("symbbit", arg, MathStyle.BLACKBOARD_BOLD) { "Blackboard bold italic mathematical alphabet (unicode-math)." }
            styleCmd("symscr", arg, MathStyle.SCRIPT) { "Script mathematical alphabet (unicode-math)." }
            styleCmd("symfrak", arg, MathStyle.FRAKTUR) { "Fraktur mathematical alphabet (unicode-math)." }
            styleCmd("symsf", arg, MathStyle.SANS_SERIF) { "Sans-serif mathematical alphabet (unicode-math)." }
            styleCmd("symsfup", arg, MathStyle.SANS_SERIF) { "Sans-serif upright mathematical alphabet (unicode-math)." }
            styleCmd("symsfit", arg, MathStyle.SANS_SERIF) { "Sans-serif italic mathematical alphabet (unicode-math)." }
            styleCmd("symtt", arg, MathStyle.MONOSPACE) { "Monospace mathematical alphabet (unicode-math)." }

            styleCmd("symbfit", arg, MathStyle.BOLD_ITALIC) { "Bold italic mathematical alphabet (unicode-math)." }
            styleCmd("symbfscr", arg, MathStyle.BOLD_SCRIPT) { "Bold script mathematical alphabet (unicode-math)." }
            styleCmd("symbffrak", arg, MathStyle.BOLD_FRAKTUR) { "Bold fraktur mathematical alphabet (unicode-math)." }
            styleCmd("symbfsfup", arg, MathStyle.BOLD_SANS_SERIF_UPRIGHT) { "Bold upright sans-serif mathematical alphabet (unicode-math)." }
            styleCmd("symbfsfit", arg, MathStyle.BOLD_SANS_SERIF_ITALIC) { "Bold italic sans-serif mathematical alphabet (unicode-math)." }
            styleCmd("symbfsf", arg, MathStyle.BOLD_SANS_SERIF_UPRIGHT) { "Bold sans-serif mathematical alphabet (unicode-math)." }
            styleCmd("symnormal", arg, MathStyle.ROMAN) { "Normal mathematical alphabet (unicode-math)." }
        }
    }
}
