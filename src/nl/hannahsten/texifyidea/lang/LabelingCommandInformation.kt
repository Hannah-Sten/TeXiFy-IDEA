package nl.hannahsten.texifyidea.lang

/**
 * Information about a custom commands which has a \label command in the definition.
 */
data class LabelingCommandInformation(
        /** Parameter positions which define a label, starting from 0 (note: LaTeX starts from 1).
         * For example in \newcommand{\mylabel}[2]{\section{#1}\label{#2}} it is position 1. */
        var positions: List<Int>,
        /** True if the first label parameter labels a previous command, for example in \newcommand{\mylabel}[1]{\label{#1}}
         * but not in \newcommand{\mylabel}[2]{\section{#1}\label{#2}} */
        var labelsPreviousCommand: Boolean,
        /** Default label prefix, for example in \newcommand{\mylabel}[1]{\label{sec:#1}} it would be sec: */
        var prefix: String = ""
)
