package nl.hannahsten.texifyidea.lang

import arrow.core.NonEmptyList

/**
 * Information about a user-defined environment which has a \label command in the definition.
 */
data class LabelingEnvironmentInformation(
    /** Parameter positions which define a label, starting from 0 (note: LaTeX starts from 1). */
    var positions: NonEmptyList<Int>,
    /** Default label prefix, for example in \newcommand{\mylabel}[1]{\label{sec:#1}} it would be sec: */
    var prefix: String = ""
)
