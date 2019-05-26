package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.util.PackageUtils
import nl.rubensten.texifyidea.util.document
import java.util.regex.Pattern

class LatexEquationReferenceInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use \\eqref{...} instead of (\\ref{...})",
        myInspectionId = "EquationReference",
        errorMessage = { "" },
        pattern = Pattern.compile("(\\(\\\\ref\\{)([\\w:]+)(}\\))"),
        quickFixName = { "Replace with \\eqref" },
        groupFetcher = { listOf(it.group(2))}
) {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>) {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return
        val reference = groups[0]

        document.replaceString(replacementRange.start, replacementRange.endInclusive, "\\eqref{$reference}")
        PackageUtils.insertUsepackage(file, Package.AMSMATH)
    }
}