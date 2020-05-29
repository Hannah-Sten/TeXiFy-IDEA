package nl.hannahsten.texifyidea.settings

data class LabelingCommandInformation(
        /** Parameter positions which define a label. */
        var positions: List<Int>,
        /** True if the first label parameter labels a previous command, for example in \newcommand{\mylabel}[1]{\label{#1}}
         * but not in \newcommand{\mylabel}[2]{\section{#1}\label{#2}} */
        var labelsPreviousCommand: Boolean
)
