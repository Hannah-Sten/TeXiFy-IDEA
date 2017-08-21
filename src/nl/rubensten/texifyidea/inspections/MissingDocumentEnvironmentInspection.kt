package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.rubensten.texifyidea.psi.LatexBeginCommand
import nl.rubensten.texifyidea.util.TexifyUtil
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class MissingDocumentEnvironmentInspection : TexifyInspectionBase() {

    override fun getDisplayName(): String {
        return "Missing document environment"
    }

    override fun getShortName(): String {
        return "MissingDocumentEnvironment"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val fileSet = TexifyUtil.getReferencedFileSet(file)
        val commandSet: MutableSet<LatexBeginCommand> = HashSet()

        for (referencedFile: PsiFile in fileSet) {
            val beginCommands = PsiTreeUtil.findChildrenOfType(referencedFile, LatexBeginCommand::class.java)
            commandSet.addAll(beginCommands)
        }

        if (!commandSet.isEmpty()) {
            return descriptors
        }

        for (beginCommand in commandSet) {
            val environment = beginCommand.parameterList[0]
            if (environment.text == "{document}") {
                return descriptors
            }
        }

        descriptors.add(
                manager.createProblemDescriptor(
                        file,
                        "Document doesn't contain a document environment.",
                        InspectionFix(),
                        ProblemHighlightType.ERROR,
                        isOntheFly
                )
        )

        return descriptors
    }

    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Add a document environment"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val psiElement = descriptor.psiElement
            val file = psiElement.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file)

            document?.insertString(document.textLength, """
                |
                |\begin{document}
                |
                |
                |
                |\end{document}""".trimMargin())
        }
    }
}
