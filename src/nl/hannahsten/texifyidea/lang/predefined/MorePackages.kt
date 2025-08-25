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
}