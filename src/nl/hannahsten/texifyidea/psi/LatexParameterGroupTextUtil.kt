package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType

fun getNameIdentifier(element: LatexParameterGroupText): PsiElement? {
    // Because we do not want to trigger the NonAsciiCharactersInspection when the LatexParameterText is not an identifier
    // (think non-ASCII characters in a \section command), we return null here when the element is not an identifier
    // It is important not to return null for any identifier, otherwise exceptions like "Throwable: null byMemberInplaceRenamer" may occur
    val command = element.firstParentOfType(LatexCommands::class)
    val environment = element.firstParentOfType(LatexEnvironment::class)
    if (Magic.Command.labelAsParameter.contains(command?.name) ||
        Magic.Environment.labelAsParameter.contains(environment?.environmentName)
    ) {
        return element
    }
    return null
}

fun setName(element: LatexParameterGroupText, name: String): PsiElement {
    val command = element.firstParentOfType(LatexCommands::class)
    val environment = element.firstParentOfType(LatexEnvironment::class)
    if (Magic.Command.labelAsParameter.contains(command?.name) || Magic.Environment.labelAsParameter.contains(
            environment?.environmentName
        )
    ) {
        val helper = LatexPsiHelper(element.project)
        helper.setOptionalParameter(command ?: environment!!.beginCommand, "label", "{$name}")
    }

    // Else, element is not renamable
    return element
}

fun getName(element: LatexParameterGroupText): String {
    return element.text ?: ""
}