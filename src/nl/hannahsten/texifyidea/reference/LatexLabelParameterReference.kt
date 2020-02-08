package nl.hannahsten.texifyidea.reference

/**
 * The difference with [LatexLabelReference] is that this reference works on normal text, i.e. the actual label parameters.
 * This means that the parameter of a \ref command will resolve to the parameter of the \label command.
 *
 * This allows us to implement find usages as well
 */
class LatexLabelParameterReference {
}