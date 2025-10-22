package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.util.findParentOfType
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.inspections.createDescriptor
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.reference.LatexLabelParameterReference
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.get
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

class LatexEquationReferenceInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "EquationReference",
) {
    override fun inspectCommand(
        command: LatexCommands, contexts: LContextSet,
        defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager,
        isOnTheFly: Boolean,
        descriptors: MutableList<ProblemDescriptor>
    ) {
        if (command.nameWithoutSlash != "ref") return
        if (!isApplicableInContexts(contexts)) return
        val labelName = command.requiredParameterText(0) ?: return
        val range = command.textRange
        val doc = file.fileDocument
        if (doc[range.startOffset - 1] != "(" || doc[range.endOffset] != ")") return
        val refLabel = LatexLabelParameterReference.multiResolve(labelName, file)
        if (refLabel.isEmpty()) return
        if (refLabel.any { res ->
                val labelElement = res.element.findParentOfType<LatexCommands>() ?: return@any false
                // if any of the labels is defined outside math environment, do not trigger
                !LatexPsiUtil.isInsideContext(labelElement, LatexContexts.Math, defBundle)
            }
        ) return
        val descriptor = manager.createDescriptor(
            command,
            "Use \\eqref for equation references",
            rangeInElement = TextRange.from(0, 4), // only highlight the command
            isOnTheFly = isOnTheFly,
            fix = ReplaceEquationReferenceQuickFix()
        )
        descriptors.add(descriptor)
    }

    private class ReplaceEquationReferenceQuickFix : LocalQuickFix {
        override fun getFamilyName(): @IntentionFamilyName String {
            return "Replace with \\eqref"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as? LatexCommands ?: return
            val range = element.textRange
            val text = element.text
            val file = element.containingFile ?: return
            val doc = file.document() ?: return
            // delete the parentheses around the command
            doc.replaceString(range.startOffset - 1, range.endOffset + 1, "\\eqref${text.substring(4)}")
            // Ensure the amsmath package is imported
            PackageUtils.insertUsePackage(file, LatexLib.AMSMATH)
        }
    }
}
