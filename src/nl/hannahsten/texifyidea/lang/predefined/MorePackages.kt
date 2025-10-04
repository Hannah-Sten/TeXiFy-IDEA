package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.PredefinedEntitySet

object MorePackages : PredefinedEntitySet() {

    val tikzcd = definitions {
        underPackage("tikzcd") {
            "tikzcd".env(LatexContexts.Alignable) {
                "Environment for commutative diagrams using TikZ."
            }
        }
    }

    val physics = definedUnder("physics") {
        val matrixArg = "mat".required(+LatexContexts.Alignable)
        underContext(LatexContexts.Math) {
            arrayOf("mqty", "pmqty", "bmqty", "Bmqty", "vmqty", "Vmqty", "dmqty").forEach {
                it.cmd(matrixArg)
            }
        }
    }

    val niceMatrix = definedUnder("nicematrix") {
        val colsArg = "cols".required(LatexContexts.Literal)
        val introAlignable = +LatexContexts.Alignable
        underContext(LatexContexts.Math) {
            arrayOf("NiceMatrix", "pNiceMatrix", "bNiceMatrix", "BNiceMatrix", "vNiceMatrix", "VNiceMatrix").forEach {
                it.env(introAlignable)
            }
            arrayOf("NiceArray", "pNiceArray", "bNiceArray", "BNiceArray", "vNiceArray", "VNiceArray").forEach {
                it.env(introAlignable, colsArg)
            }
        }

        underContext(LatexContexts.Text) {
            arrayOf("NiceTabular", "NiceTabular*").forEach {
                it.env(setOf(LatexContexts.Tabular, LatexContexts.Text), colsArg)
            }
        }
    }
}