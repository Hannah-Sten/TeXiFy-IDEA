package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project

/**
 * @author Ruben Schellekens
 */
object NoQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = ""
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) = Unit
}