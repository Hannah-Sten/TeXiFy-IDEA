package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.findLabels
import nl.rubensten.texifyidea.util.findOuterMathEnvironment
import nl.rubensten.texifyidea.util.insertUsepackage
import java.util.regex.Pattern

open class LatexEquationReferenceInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use of (\\ref{...}) instead of \\eqref{...}",
        myInspectionId = "EquationReference",
        errorMessage = { "Use \\eqref" },
        pattern = Pattern.compile("(\\(\\\\ref\\{)([\\w:]+)(}\\))"),
        quickFixName = { "Replace with \\eqref" },
        groupFetcher = { listOf(it.group(2)) },
        cancelIf = { matcher, psiFile ->
            // Cancel if the label was defined outside a math environment.
            psiFile.findLabels().find { it.text == "\\label{${matcher.group(2)}}" }.findOuterMathEnvironment() == null
        }
) {

    override fun applyFix(descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return 0
        val reference = groups[0]

        document.replaceString(replacementRange.start, replacementRange.endInclusive, "\\eqref{$reference}")
        // We add two characters: 'eq' and remove the two braces, thus the document gets 0 characters longer.
        return 0
    }

    override fun applyFixes(descriptor: ProblemDescriptor, replacementRanges: List<IntRange>, replacements: List<String>, groups: List<List<String>>) {
        super.applyFixes(descriptor, replacementRanges, replacements, groups)

        // We overrided applyFixes instead of applyFix because all fixes need to be applied together, and only after that we insert any required package.
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(Package.AMSMATH)
    }
}