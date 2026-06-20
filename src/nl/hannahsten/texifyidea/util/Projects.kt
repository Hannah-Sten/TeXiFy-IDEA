package nl.hannahsten.texifyidea.util

import com.intellij.execution.RunManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.serviceContainer.AlreadyDisposedException
import com.intellij.util.concurrency.annotations.RequiresReadLock
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.modules.LatexModuleType
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationType
import nl.hannahsten.texifyidea.util.files.allChildFiles

/**
 * Get a project [GlobalSearchScope] for this project.
 */
val Project.projectSearchScope: GlobalSearchScope
    get() = GlobalSearchScope.projectScope(this)

val Project.contentSearchScope: GlobalSearchScope
    get() = ProjectScope.getContentScope(this)

@Suppress("unused")
val Project.librarySearchScope: GlobalSearchScope
    get() = ProjectScope.getLibrariesScope(this)

/**
 * Get a project [GlobalSearchScope] for this project.
 */
@Suppress("unused")
val Project.everythingScope: GlobalSearchScope
    get() = GlobalSearchScope.everythingScope(this)

/**
 * Get a [GlobalSearchScope] for the source folders in this project.
 */
val Project.sourceSetSearchScope: GlobalSearchScope
    get() {
        val rootManager = ProjectRootManager.getInstance(this)
        val files = rootManager.contentSourceRoots.asSequence()
            .flatMap { it.allChildFiles().asSequence() }
            .toSet()
        return GlobalSearchScope.filesWithoutLibrariesScope(this, files)
    }

/**
 * Looks for all defined document classes in the project.
 */
fun Project.findAvailableDocumentClasses(): Set<String> {
    val defines = NewCommandsIndex.getByName("ProvidesClass", this, sourceSetSearchScope)
    return defines.asSequence()
        .map { it.requiredParametersText() }
        .filter { it.isNotEmpty() }
        .mapNotNull { it.firstOrNull() }
        .toSet()
}

/**
 * Whether the project contains a file of the given [FileType].
 */
fun Project.containsFileOfType(type: FileType): Boolean {
    if (!isInitialized) return false
    try {
        val scope = GlobalSearchScope.projectScope(this)
        return FileTypeIndex.containsFileOfType(type, scope)
    }
    catch (e: IllegalStateException) {
        // Doesn't happen very often, and afaik there's no proper way of checking whether this index is initialized. See #2855
        if (e.message?.contains("Index is not created for `filetypes`") == true) {
            return false
        }
        else {
            throw e
        }
    } catch (_: IndexNotReadyException) {
        return false
    }
}

/**
 * Get all LaTeX run configurations in the project.
 */
fun Project.getLatexRunConfigurations(): Collection<LatexRunConfiguration> {
    if (isDisposed) return emptyList()
    return RunManager.getInstance(this).allConfigurationsList.filterIsInstance<LatexRunConfiguration>()
}

fun Project.hasLatexRunConfigurations(): Boolean {
    if (isDisposed) return false
    return RunManager.getInstance(this).allConfigurationsList.any { it is LatexRunConfiguration }
}

/**
 * Get the run configuration that is currently selected.
 */
fun Project?.selectedRunConfig(): LatexRunConfiguration? = this?.let {
    RunManager.getInstance(it).selectedConfiguration?.configuration as? LatexRunConfiguration
}

/**
 * Get the run configuration of the template.
 */
fun Project?.latexTemplateRunConfig(): LatexRunConfiguration? = this?.let {
    val runManager = RunManager.getInstance(it)
    runManager.getConfigurationTemplate(LatexConfigurationFactory(LatexRunConfigurationType())).configuration as? LatexRunConfiguration
}

/**
 * Gets the currently focused text editor (returns null for example if the user has clicked on some button)
 */
fun Project.focusedTextEditor(): TextEditor? = FileEditorManager.getInstance(this).focusedEditor as? TextEditor?

/**
 * Gets the text editor which was last selected (may not be focused anymore)
 */
fun Project.selectedTextEditor(): TextEditor? = FileEditorManager.getInstance(this).selectedEditor as? TextEditor?

fun Project.selectedTextEditorOrWarning(): TextEditor? {
    selectedTextEditor()?.let { return it }
    Notification("LaTeX", "Could not find an open editor to insert text", "Put your caret in a LaTeX file first. Please report an issue on GitHub if you believe this is incorrect", NotificationType.ERROR).notify(this)
    return null
}

/**
 * Checks if there is a LaTeX module in this project.
 *
 * Note: according to the documentation of ModuleType:
 *     If you need to make an action enabled in presence of a specific technology only, do this by looking for required files in the project
 *     directories, not by checking type of the current module.
 */
fun Project.hasLatexModule(): Boolean {
    if (isDisposed) return false
    return try {
        ModuleManager.getInstance(this).modules
            .any { ModuleType.get(it).id == LatexModuleType.ID }
    }
    catch (_: AlreadyDisposedException) {
        false
    }
}

/**
 * Best guess at whether this project can be considered a project containing significant LaTeX things.
 */
@RequiresReadLock // Required by containsFileOfType
fun Project.isLatexProject(): Boolean = hasLatexModule() ||
    getLatexRunConfigurations().isNotEmpty() ||
    (ApplicationNamesInfo.getInstance().scriptName != "idea" && containsFileOfType(LatexFileType))

/**
 * True if we are probably in a unit test.
 */
fun isTestProject() = ApplicationManager.getApplication().isUnitTestMode
