@file:Suppress("unused")

package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.editor.folding.MathStyle
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet

object PredefinedCmdMath : PredefinedCommandSet() {

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
        "mathbf".cmd(arg) { "Bold" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.BOLD)
        }
        "mathcal".cmd(arg) { "Calligraphic" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.CALLIGRAPHIC)
        }

        "mathit".cmd(arg) { "Italic" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.ITALIC)
        }
        "mathnormal".cmd(arg) { "Normal" }
        "mathsf".cmd(arg) { "Sans-serif" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.SANS_SERIF)
        }
        "mathrm".cmd(arg) { "Roman" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.ROMAN)
        }
        "mathscr".cmd(arg) { "Script" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.SCRIPT)
        }
        "mathtt".cmd(arg) { "Typewriter" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.MONOSPACE)
        }

        packageOf("amsmath")
        "boldsymbol".cmd(arg) { "Bold symbols" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.BOLD)
        }

        packageOf("amsfonts")
        "mathbb".cmd(arg) { "Blackboard bold" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.BLACKBOARD_BOLD)
        }
        "mathfrak".cmd(arg) { "Fraktur" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.FRAKTUR)
        }

        packageOf("bbm")
        "mathbbm".cmd(arg) { "Blackboard bold (bbm)" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.BLACKBOARD_BOLD)
        }
        "mathbbmss".cmd(arg) { "Blackboard bold sans-serif" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.BLACKBOARD_BOLD)
        }
        "mathbbmtt".cmd(arg) { "Blackboard bold typewriter" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.BLACKBOARD_BOLD)
        }

        packageOf("bm")
        "bm".cmd(arg) { "Bold math" }.apply {
            putMeta(MathStyle.META_KEY, MathStyle.BOLD)
        }

        packageOf("dsfont")
        "mathds".cmd(arg) { "Double-struck" }
    }

    val unicodeMathStyleCommands = mathCommands {
        val arg = "text".required

        underPackage("unicode-math") {
            "symup".cmd(arg) { "Upright mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.ROMAN)
            }
            "symit".cmd(arg) { "Italic mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.ITALIC)
            }
            "symbf".cmd(arg) { "Bold mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.BOLD)
            }
            "symcal".cmd(arg) { "Calligraphic mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.CALLIGRAPHIC)
            }
            "symbb".cmd(arg) { "Blackboard bold mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.BLACKBOARD_BOLD)
            }
            "symscr".cmd(arg) { "Script mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.SCRIPT)
            }
            "symfrak".cmd(arg) { "Fraktur mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.FRAKTUR)
            }
            "symsf".cmd(arg) { "Sans-serif mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.SANS_SERIF)
            }
            "symtt".cmd(arg) { "Monospace mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.MONOSPACE)
            }

            "symbfit".cmd(arg) { "Bold italic mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.BOLD_ITALIC)
            }
            "symbfscr".cmd(arg) { "Bold script mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.BOLD_SCRIPT)
            }
            "symbffrak".cmd(arg) { "Bold fraktur mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.BOLD_FRAKTUR)
            }
            "symbfsfup".cmd(arg) { "Bold upright sans-serif mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.BOLD_SANS_SERIF_UPRIGHT)
            }
            "symbfsfit".cmd(arg) { "Bold italic sans-serif mathematical alphabet (unicode-math)." }.apply {
                putMeta(MathStyle.META_KEY, MathStyle.BOLD_SANS_SERIF_ITALIC)
            }
        }
    }
}
