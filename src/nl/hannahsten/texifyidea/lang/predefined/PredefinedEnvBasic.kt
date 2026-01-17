package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexContexts.Alignable
import nl.hannahsten.texifyidea.lang.LatexContexts.Literal
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.PredefinedEnvironmentSet

object PredefinedEnvBasic : PredefinedEnvironmentSet() {

    val basic = buildEnvironments {
        val mainTextIntro = LatexContextIntro.Modify(
            toAdd = setOf(LatexContexts.Text),
            toRemove = setOf(LatexContexts.Preamble)
        )
        EnvironmentNames.DOCUMENT.env(mainTextIntro) {
            "The main document environment."
        }

        +"Huge"
        +"LARGE"
        +"Large"
        +"Verbatim"

        underContext(LatexContexts.Text) {
            "abstract".env(LatexContexts.Text)
        }

        +"algorithm"
        +"alltt"
        +"center"
        +"description"
        +"filecontents"
        +"filecontents*"
        +"flushleft"
        +"flushright"
        +"footnotesize"
        +"huge"
        +"large"
        +"lrbox"

        +"normalsize"
        +"quotation"
        +"quote"
        +"scriptsize"
        +"small"
        +"tabbing"

        +"theindex"
        +"tiny"
        +"titlepage"
        +"trivlist"
        +"verse"

        "thebibliography".env(LatexContextIntro.inherit(), "widestlabel".required) {
            "A bibliography environment."
        }
    }

    val lists = buildEnvironments {
        "enumerate".env(+LatexContexts.Enumerate) {
            "A numbered list."
        }
        "itemize".env(+LatexContexts.Enumerate) {
            "A bulleted list."
        }
        "list".env(LatexContextIntro.inherit(), "label".required, "spacing".required)
    }

    val basicIntroMath = buildEnvironments {
        "math".env(LatexContextIntro.INLINE_MATH) { "Inline math mode." }
        "displaymath".env(LatexContexts.Math) { "Display math mode." }

        val alignableMath = setOf(LatexContexts.Math, Alignable)
        "eqnarray".env(alignableMath) { "A set of aligned equations, similar to `align`." }
        "eqnarray*".env(alignableMath) { "A set of aligned equations, similar to `align*`." }
    }

    val basicUnderMath = buildEnvironments {
        underContext(LatexContexts.Math) {
            "array".env(setOf(LatexContexts.Math, Alignable), "cols".required) {
                "An array (matrix) environment."
            }
        }
    }

    val verbatim = buildEnvironments {
        +"verbatim"
        +"verbatim*"
    }

    val someMathEnv = buildEnvironments {

        val alignableMath = setOf(LatexContexts.Math, Alignable)
        val math = setOf(LatexContexts.Math)

        underContext(LatexContexts.Text) {
            // equation is basic
            "equation".env(math) { "A numbered equation." }

            underPackage("mathpartir") {
                "mathpar".env(math)
            }
        }

        packageOf("amsmath")

        underContext(LatexContexts.Text) {
            "equation*".env(math) { "An unnumbered equation." }
            "align".env(alignableMath) { "A set of aligned equations." }
            "align*".env(alignableMath) { "A set of aligned equations without numbering." }
            "gather".env(math) { "A set of equations, centered." }
            "gather*".env(math) { "A set of equations, centered, without numbering." }
            "multline".env(math) { "A long equation that spans multiple lines." }
            "multline*".env(math) { "A long equation that spans multiple lines, without numbering." }
            "flalign".env(math) { "A set of equations, left and right aligned." }
            "flalign*".env(math) { "A set of equations, left and right aligned, without numbering." }
        }

        underContext(LatexContexts.Math) {
            val alignableEnvList = listOf(
                "alignat", "alignat*", "aligned", "alignedat", "xalignat", "xalignat*", "xxalignat",
                "cases",
                "Bmatrix", "Vmatrix", "bmatrix", "matrix", "pmatrix", "smallmatrix", "vmatrix",
                "split", "subarray", "subequations",
            )
            val introAlignable = +Alignable
            alignableEnvList.forEach { envName ->
                envName.env(introAlignable)
            }
            +"gathered"
        }
    }

    val mathtoolsUnderMath = buildEnvironments {
        packageOf("mathtools")
        val introAlignable = +Alignable
        underContext(LatexContexts.Math) {
            val moreMatrices = listOf(
                "Bmatrix*", "Bsmallmatrix", "Bsmallmatrix*", "Vmatrix*", "Vsmallmatrix", "Vsmallmatrix*",
                "bmatrix*", "bsmallmatrix", "bsmallmatrix*", "matrix*", "pmatrix*", "psmallmatrix",
                "psmallmatrix*", "smallmatrix*", "vmatrix*", "vsmallmatrix", "vsmallmatrix*"
            )
            moreMatrices.forEach { envName ->
                envName.env(introAlignable)
            }
        }
    }

    val additionalUnderMath = buildEnvironments {
        applicableIn(LatexContexts.Math)
        underPackage("gauss") {
            "gmatrix".env(+Alignable)
        }
    }

    val thmAndProof = buildEnvironments {
        "theorem".env(LatexContexts.Text, "name".optional(LatexContexts.Text)) {
            "A theorem environment."
        }
        packageOf("amsthm")
        +"proof"
    }

    val optidef = buildEnvironments {
        packageOf("optidef")
        "argmaxi".env(LatexContexts.Math)
        "argmaxi*".env(LatexContexts.Math)
        "argmaxi!".env(LatexContexts.Math)
        "argmini".env(LatexContexts.Math)
        "argmini*".env(LatexContexts.Math)
        "argmini!".env(LatexContexts.Math)
        "maxi".env(LatexContexts.Math)
        "maxi*".env(LatexContexts.Math)
        "maxi!".env(LatexContexts.Math)
        "mini".env(LatexContexts.Math)
        "mini*".env(LatexContexts.Math)
        "mini!".env(LatexContexts.Math)
    }

    val comment = buildEnvironments {
        packageOf("comment")
        "comment".env(LatexContexts.Comment)
    }

    val figuresAndTable = buildEnvironments {
        val placement = "placement".optional(Literal)
        "figure".env(LatexContexts.Figure, placement) {
            "A figure environment."
        }
        "figure*".env(LatexContexts.Figure, placement)

        "table".env(+LatexContexts.Table, placement) {
            "A table environment."
        }
        "table*".env(+LatexContexts.Table, placement)

        val cols = "cols".required(Literal)
        val pos = "pos".optional(Literal)
        val width = "width".optional(Literal)
        "tabular".env(+LatexContexts.Tabular, pos, cols) {
            "A basic table."
        }
        "tabular*".env(+LatexContexts.Tabular, width, pos, cols)
        "tabularx".env(+LatexContexts.Tabular, width, cols)
        "tabulary".env(+LatexContexts.Tabular, "length".required(Literal), "pream".optional(Literal))
        "longtable".env(LatexContexts.Table, cols)

        underPackage("tabularray") {
            "longtblr".env(+LatexContexts.Table, "outer".optional, "inner".required)
            "talltblr".env(+LatexContexts.Table, "outer".optional, "inner".required)
            "tblr".env(+LatexContexts.Table, "outer".optional, "inner".required)
        }

        underPackage("blkarray") {
            "blockarray".env(+Alignable, "cols".required(Literal)) {
                "A block array environment."
            }
        }
    }

    val formatting = buildEnvironments {
        "minipage".env(LatexContexts.Text, "position".optional(Literal), "width".required(Literal)) {
            "A minipage environment."
        }
    }

    val frames = buildEnvironments {
        underPackage(LatexLib.Class("beamer")) {
            "frame".env(LatexContexts.Text, "title".required(LatexContexts.Text)) {
                "A frame environment."
            }
            "block".env(LatexContexts.Text, "title".required(LatexContexts.Text)) {
                "A block environment."
            }
        }
    }

    val algorithm = buildEnvironments {
        packageOf("algorithmicx")
        +"algorithmic"
    }

    val other = buildEnvironments {
        // contexts can be further specified
        packageOf("listings")
        +"lstlisting"

        packageOf("luacode")
        +"luacode"
        +"luacode*"

        packageOf("minted")
        +"minted"

        packageOf("pythontex")
        +"pyblock"
        +"pycode"
        +"pyconsole"
        +"pysub"
        +"pyverbatim"

        packageOf("xcolor")
        "testcolors".env(LatexContexts.Text, "num models".optional) //

        packageOf("tikz")
        "tikzpicture".env(LatexContexts.TikzPicture)

        packageOf("widetable")
        +"widetable"
        +"widetabular"

        packageOf("citation-style-language")
        +"refsection"
    }
}