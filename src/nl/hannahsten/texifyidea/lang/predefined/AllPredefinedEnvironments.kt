package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexContexts.Literal
import nl.hannahsten.texifyidea.lang.LatexContexts.Math
import nl.hannahsten.texifyidea.lang.PredefinedEnvironmentSet

object AllPredefinedEnvironments : PredefinedEnvironmentSet() {

    val basic = buildEnvironments {
        "document".env(LatexContexts.Text) {
            "The main document environment."
        }

        +"Huge"
        +"LARGE"
        +"Large"
        +"Verbatim"
        +"abstract"
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
        "itemize".env(+LatexContexts.Enumerate)

        +"array"

        "list".env(LatexContextIntro.inherit(), "label".required, "spacing".required)
    }
    val basicIntroMath = buildEnvironments {
        "math".env(Math) { "Inline math mode." }
        "displaymath".env(Math) { "Display math mode." }

        "eqnarray".env(Math) { "A set of aligned equations, similar to `align`." }
        "eqnarray*".env(Math) { "A set of aligned equations, similar to `align*`." }
    }

    val basicUnderMath = buildEnvironments {
        underContext(Math) {
            "array".env(Math, "cols".required) {
                "An array (matrix) environment."
            }
        }
    }

    val verbatim = buildEnvironments {
        +"verbatim"
        +"verbatim*"
    }

    val amsmathEnv = buildEnvironments {
        packageOf("amsmath")

        underContext(LatexContexts.Text) {
            "equation".env(Math) { "A numbered equation." }
            "equation*".env(Math) { "An unnumbered equation." }
            "align".env(Math) { "A set of aligned equations." }
            "align*".env(Math) { "A set of aligned equations without numbering." }
            "gather".env(Math) { "A set of equations, centered." }
            "gather*".env(Math) { "A set of equations, centered, without numbering." }
            "multline".env(Math) { "A long equation that spans multiple lines." }
            "multline*".env(Math) { "A long equation that spans multiple lines, without numbering." }
            "flalign".env(Math) { "A set of equations, left and right aligned." }
            "flalign*".env(Math) { "A set of equations, left and right aligned, without numbering." }
        }



        underContext(Math) {
            +"Bmatrix"
            +"Vmatrix"
            +"alignat"
            +"alignat*"
            +"aligned"
            +"alignedat"
            +"bmatrix"
            +"cases"
            +"gathered"
            +"matrix"
            +"pmatrix"
            +"smallmatrix"
            +"split"
            +"subarray"
            +"subequations"
            +"vmatrix"
            +"xalignat"
            +"xalignat*"
            +"xxalignat"
        }
    }

    val mathtoolsUnderMath = buildEnvironments {
        packageOf("mathtools")
        underContext(Math) {
            +"Bmatrix*"
            +"Bsmallmatrix"
            +"Bsmallmatrix*"
            +"Vmatrix*"
            +"Vsmallmatrix"
            +"Vsmallmatrix*"
            +"bmatrix*"
            +"bsmallmatrix"
            +"bsmallmatrix*"
            +"matrix*"
            +"pmatrix*"
            +"psmallmatrix"
            +"psmallmatrix*"
            +"smallmatrix*"
            +"vmatrix*"
            +"vsmallmatrix"
            +"vsmallmatrix*"
        }
    }

    val additionalUnderMath = buildEnvironments {
        setRequiredContext(Math)
        underPackage("gauss") {
            +"gmatrix"
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
        "argmaxi".env(Math)
        "argmaxi*".env(Math)
        "argmaxi!".env(Math)
        "argmini".env(Math)
        "argmini*".env(Math)
        "argmini!".env(Math)
        "maxi".env(Math)
        "maxi*".env(Math)
        "maxi!".env(Math)
        "mini".env(Math)
        "mini*".env(Math)
        "mini!".env(Math)
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
    }

    val formatting = buildEnvironments {
        "minipage".env(LatexContexts.Text, "position".optional(Literal), "width".required(Literal)) {
            "A minipage environment."
        }
    }

    val beamer = buildEnvironments {
        packageOf("beamer")
        +"frame"
        +"frame*"
    }

    val algorithm = buildEnvironments {
        packageOf("algorithmicx")
        +"algorithmic"
        packageOf("blkarray")
        +"block"
        +"blockarray"
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
        +"tikzpicture"

        packageOf("widetable")
        +"widetable"
        +"widetabular"
    }

    val packageToEnvironments: Map<String, List<LSemanticEnv>> = allEnvironments.groupBy { it.dependency }

    val simpleNameLookup = allEnvironments.associateBy { it.name }
}