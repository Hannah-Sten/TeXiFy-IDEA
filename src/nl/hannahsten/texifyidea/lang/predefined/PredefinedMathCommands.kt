package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

object PredefinedMathCommands : PredefinedCommandSet() {

    val textMathCommands = buildCommands {
        packageOf("amsmath")
        underPackage("amstext") {
            underContext(LatexContexts.Math) {
                "text".cmd("text".required(LatexContexts.Text)) { "Text in math" }
            }
        }

        underContext {
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
        "intertext".cmd("text".required) { "Intertext" }
        "overleftrightarrow".cmd("text".required) { "Over left right arrow" }
        "underleftarrow".cmd("text".required) { "Under left arrow" }
        "underleftrightarrow".cmd("text".required) { "Under left right arrow" }
        "underrightarrow".cmd("text".required) { "Under right arrow" }
        "xleftarrow".cmd("text".required) { "Above left arrow" }
        "xrightarrow".cmd("text".required) { "Above right arrow" }
    }

    val fontCommands = mathCommands {
        val arg = "text".required
        "mathbf".cmd(arg) { "Bold" }
        "mathcal".cmd(arg) { "Calligraphic" }
        "mathds".cmd(arg) { "Double-struck" }
        "mathit".cmd(arg) { "Italic" }
        "mathnormal".cmd(arg) { "Normal" }
        "mathsf".cmd(arg) { "Sans-serif" }
        "mathrm".cmd(arg) { "Roman" }
        "mathscr".cmd(arg) { "Script" }
        "mathtt".cmd(arg) { "Typewriter" }

        packageOf("amsfonts")

        "mathbb".cmd(arg) { "Blackboard bold" }
        "mathfrak".cmd(arg) { "Fraktur" }

        packageOf("bm")
        "bm".cmd(arg) { "Bold math" }
    }
}