package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.ProblemDescriptor
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.insertUsepackage
import nl.hannahsten.texifyidea.util.labels.findLatexLabelingElementsInFileSet
import nl.hannahsten.texifyidea.util.parser.findOuterMathEnvironment
import java.util.regex.Pattern

open class LatexEquationReferenceInspection : TexifyRegexInspection(
    inspectionDisplayName = "Use of (\\ref{...}) instead of \\eqref{...}",
    inspectionId = "EquationReference",
    errorMessage = { "Use \\eqref" },
    pattern = Pattern.compile("(\\(\\\\ref\\{)([\\w:]+)(}\\))"),
    replacement = { matcher, _ -> "\\eqref{${matcher.group(2)}}" },
    replacementRange = { it.groupRange(0) },
    quickFixName = { "Replace with \\eqref" },
    groupFetcher = { listOf(it.group(2)) },
    cancelIf = { matcher, psiFile ->
        // Cancel if the label was defined outside a math environment.
        psiFile.findLatexLabelingElementsInFileSet().find { it.text == "\\label{${matcher.group(2)}}" }.findOuterMathEnvironment() == null
    }
) {

    override fun applyFixes(descriptor: ProblemDescriptor, replacementRanges: List<IntRange>, replacements: List<String>, groups: List<List<String>>) {
        super.applyFixes(descriptor, replacementRanges, replacements, groups)

        // We overrode applyFixes instead of applyFix because all fixes need to be applied together, and only after that we insert any required package.
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(LatexPackage.AMSMATH)
    }
}