package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.util.PackageUtils
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.insertUsepackage
import java.util.regex.Pattern

class LatexEquationReferenceInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use of (\\ref{...}) instead of \\eqref{...}",
        myInspectionId = "EquationReference",
        errorMessage = { "Use \\eqref" },
        pattern = Pattern.compile("(\\(\\\\ref\\{)([\\w:]+)(}\\))"),
        quickFixName = { "Replace with \\eqref" },
        groupFetcher = { listOf(it.group(2))}
) {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return 0
        val reference = groups[0]

        document.replaceString(replacementRange.start, replacementRange.endInclusive, "\\eqref{$reference}")
        // We add two characters: 'eq' and remove the two braces, thus the document gets 0 characters longer.
        return 0
    }

    override fun applyFixes(project: Project, descriptor: ProblemDescriptor, replacementRanges: List<IntRange>, replacements: List<String>, groups: List<List<String>>) {
        super.applyFixes(project, descriptor, replacementRanges, replacements, groups)

        // We overrided applyFixes instead of applyFix because all fixes need to be applied together, and only after that we insert any required package.
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(Package.AMSMATH)
    }
}