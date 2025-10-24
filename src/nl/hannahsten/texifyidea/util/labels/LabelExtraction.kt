
/**
 * Returns -1 if the command does not define labels
 */
fun LatexCommands.getLabelPosition(bundle: DefinitionBundle): Int {
    // Skip labels in command definitions
    val contexts = LatexPsiUtil.resolveContextUpward(this, bundle)
    if (LatexContexts.InsideDefinition in contexts || LatexContexts.CommandDeclaration in contexts) return -1

    return LatexDefinitionService.resolveCommand(this)?.arguments?.indexOfFirst { it.contextSignature.introduces(LatexContexts.LabelDefinition) } ?: -1
}

fun LatexCommands.isLabelCommand(bundle: DefinitionBundle) = getLabelPosition(bundle) >= 0
