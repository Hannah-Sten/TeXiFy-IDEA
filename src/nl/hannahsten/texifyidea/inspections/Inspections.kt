package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ex.InspectionToolRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * Map where each [InsightGroup] is mapped to all the relevant inspection ids.
 */
val ALL_TEXIFY_INSPECTIONS: Map<InsightGroup, List<String>> by lazy {
    val inspections = InspectionToolRegistrar.getInstance().createTools()
    val insightGroups = InsightGroup.entries.toTypedArray()
    HashMap<InsightGroup, List<String>>().apply {
        for (group in insightGroups) {
            val groupInspections = ArrayList<String>()
            for (inspection in inspections) {
                if (inspection.shortName.startsWith(group.prefix)) {
                    groupInspections.add(inspection.shortName.substring(group.prefix.length))
                }
            }
            put(group, groupInspections)
        }
    }
}

/**
 * Create a problem descriptor with the given parameters.
 * This kotlin function enables default parameters and named arguments, making the code more readable.
 *
 * @param element The PSI element where the problem is located.
 * @param descriptionTemplate The description of the problem.
 * @param highlightType The type of highlighting for the problem.
 * @param isOnTheFly Whether the inspection is being run on-the-fly.
 * @param fix An optional quick fix to be associated with the problem.
 * @return A [ProblemDescriptor] representing the identified problem.
 */
fun InspectionManager.createDescriptor(
    element: PsiElement,
    descriptionTemplate: String,
    isOnTheFly: Boolean,
    highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    rangeInElement: TextRange? = null,
    fix: LocalQuickFix? = null,
): ProblemDescriptor {
    val fixes = if(fix == null) LocalQuickFix.EMPTY_ARRAY else arrayOf(fix)
    return createProblemDescriptor(
        element,
        rangeInElement,
        descriptionTemplate,
        highlightType,
        isOnTheFly,
        *fixes
    )
}

fun InspectionManager.createDescriptor(
    element: PsiElement,
    descriptionTemplate: String,
    isOnTheFly: Boolean,
    highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    rangeInElement: TextRange? = null,
    fixes: Array<LocalQuickFix>
): ProblemDescriptor {
    return createProblemDescriptor(
        element,
        rangeInElement,
        descriptionTemplate,
        highlightType,
        isOnTheFly,
        *fixes
    )
}