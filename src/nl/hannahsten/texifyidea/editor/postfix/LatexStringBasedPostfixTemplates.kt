package nl.hannahsten.texifyidea.editor.postfix

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.codeInsight.template.postfix.templates.StringBasedPostfixTemplate
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.LatexPackage

/**
 * When adding a template that inserts a command that requires a package, make
 * sure that there exists a [LatexPostfixTemplateFromPackageProvider] for this
 * package, and add it to [LatexPostfixTemplateFromPackageProvider.getProvider].
 * Don't forget to add this provider to plugin.xml.
 */

/* General wrappers. */
internal object LatexWrapWithInlineMathPostfixTemplate : ConstantStringBasedPostfixTemplate(
    "math",
    "\$expr\$",
    "$$\$expr$$$\$END$",
    textOnly = true
)

internal object LatexWrapWithGroupPostfixTemplate : ConstantStringBasedPostfixTemplate(
    "braces",
    "{expr}",
    "{\$expr$}\$END$"
)

internal object LatexWrapWithOpenGroupPostfixTemplate : ConstantStringBasedPostfixTemplate(
    "brackets",
    "[expr]",
    "[\$expr$]\$END$"
)

internal object LatexWrapWithTextPostfixTemplate : ConstantStringBasedPostfixTemplate(
    "text",
    "\\text{expr}",
    "\\text{\$expr$\$more$}\$END$",
    mathOnly = true,
    pack = LatexPackage.AMSMATH
) {

    override fun setVariables(template: Template, element: PsiElement) {
        template.addVariable("more", "", "", true)
    }
}

/* Wrap with text command postfix template. */
internal object LatexWrapWithBoldFacePostfixTemplate : LatexWrapWithCommandPostfixTemplate("textbf", name = "bf", textOnly = true)
internal object LatexWrapWithItalicFacePostfixTemplate : LatexWrapWithCommandPostfixTemplate("textit", name = "it", textOnly = true)
internal object LatexWrapWithEmphPostfixTemplate : LatexWrapWithCommandPostfixTemplate("emph", textOnly = true)
internal object LatexWrapWithTypewriterPostfixTemplate : LatexWrapWithCommandPostfixTemplate("texttt", name = "tt", textOnly = true)

/* Wrap with math command postfix templates. */
internal object LatexWrapWithBarPostfixTemplate : LatexWrapWithCommandPostfixTemplate("bar", mathOnly = true)
internal object LatexWrapWithHatPostfixTemplate : LatexWrapWithCommandPostfixTemplate("hat", mathOnly = true)
internal object LatexWrapWithTildePostfixTemplate : LatexWrapWithCommandPostfixTemplate("tilde", mathOnly = true)
internal object LatexWrapWithSquareRootPostfixTemplate : LatexWrapWithCommandPostfixTemplate("sqrt", mathOnly = true)
internal object LatexWrapWithOverlinePostfixTemplate : LatexWrapWithCommandPostfixTemplate("overline", mathOnly = true)
internal object LatexWrapWithUnderlinePostfixTemplate : LatexWrapWithCommandPostfixTemplate("underline", mathOnly = true)
internal object LatexWrapWithMathbbPostfixTemplate : LatexWrapWithCommandPostfixTemplate("mathbb", name = "bb", mathOnly = true, pack = LatexPackage.AMSFONTS)
internal object LatexWrapWithBmPostfixTemplate : LatexWrapWithCommandPostfixTemplate("bm", mathOnly = true, pack = LatexPackage.BM)
internal object LatexWrapWithMathcalPostfixTemplate : LatexWrapWithCommandPostfixTemplate("mathcal", name = "cal", mathOnly = true)

internal open class LatexWrapWithCommandPostfixTemplate(commandName: String, name: String = commandName, mathOnly: Boolean = false, textOnly: Boolean = false, pack: LatexPackage? = null) : ConstantStringBasedPostfixTemplate(
    name,
    "\\$commandName{expr}",
    "\\$commandName{\$expr$}\$END$",
    mathOnly, textOnly, pack
)

internal abstract class ConstantStringBasedPostfixTemplate(
    name: String,
    desc: String,
    private val template: String,
    mathOnly: Boolean = false,
    textOnly: Boolean = false,
    pack: LatexPackage? = null,
    provider: PostfixTemplateProvider = LatexPostfixTemplateFromPackageProvider.getProvider(pack)
) : StringBasedPostfixTemplate(name, desc, LatexPostfixExpressionSelector(mathOnly, textOnly), provider) {

    override fun getTemplateString(element: PsiElement) = template

    override fun getElementToRemove(expr: PsiElement?) = expr
}