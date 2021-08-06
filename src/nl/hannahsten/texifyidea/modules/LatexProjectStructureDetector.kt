package nl.hannahsten.texifyidea.modules

import com.intellij.ide.projectWizard.ProjectSettingsStep
import com.intellij.ide.util.importProject.ModuleDescriptor
import com.intellij.ide.util.importProject.ProjectDescriptor
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SdkSettingsStep
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.util.Condition
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import java.io.File
import javax.swing.Icon

/**
 * Support 'create project from existing sources.'
 * Docs: https://plugins.jetbrains.com/docs/intellij/project-wizard.html#implementing-project-structure-detector
 */
class LatexProjectStructureDetector : ProjectStructureDetector() {

    override fun detectRoots(
        dir: File,
        children: Array<out File>,
        base: File,
        result: MutableList<DetectedProjectRoot>
    ): DirectoryProcessingResult {
        // We only add a LaTeX module when there are no other modules,
        // so we create it on the project root to make the Project view work (it apparently needs at least one top-level module?)
        if (children.any { it.extension == LatexFileType.defaultExtension }) {
            result.add(object : DetectedProjectRoot(base) {
                override fun getRootTypeName() = "LaTeX"
            })
            return DirectoryProcessingResult.SKIP_CHILDREN
        }
        return DirectoryProcessingResult.PROCESS_CHILDREN
    }

    override fun createWizardSteps(
        builder: ProjectFromSourcesBuilder?,
        projectDescriptor: ProjectDescriptor?,
        stepIcon: Icon?
    ): MutableList<ModuleWizardStep> {

        // Note that at least one DetectedProjectRoot needs to be added as above for the steps to be actually added
        // (to avoid showing the step for non-LaTeX project creations)

        // Custom step with TeXiFy-specific options
        // At the moment it contains no settings which are relevant for creation from existing sources, so we leave it out
//        val customStep = LatexModuleWizardStep(LatexModuleBuilder())

        // Step to set up SDK
        val filter = Condition { id: SdkTypeId -> id is LatexSdk }
        val sdkSettingsStep = ProjectSettingsStep(builder?.context)
        val sdkSetupStep = SdkSettingsStep(sdkSettingsStep, LatexModuleBuilder(), filter)
        return mutableListOf(sdkSetupStep)
    }

    override fun setupProjectStructure(
        roots: MutableCollection<DetectedProjectRoot>,
        projectDescriptor: ProjectDescriptor,
        builder: ProjectFromSourcesBuilder
    ) {
        if (projectDescriptor.modules.isEmpty() && !builder.hasRootsFromOtherDetectors(this)) {
            val modules = roots.map {
                ModuleDescriptor(it.directory, LatexModuleType.INSTANCE, emptyList())
            }
            projectDescriptor.modules = modules
        }
    }

    override fun getDetectorId() = "LaTeX"
}